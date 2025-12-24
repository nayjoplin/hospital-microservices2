package com.hospital.agendamento.service;

import com.hospital.agendamento.dto.ConsultaRequestDTO;
import com.hospital.agendamento.dto.ResponseDTO;
import com.hospital.agendamento.entity.Consulta;
import com.hospital.agendamento.entity.Paciente;
import com.hospital.agendamento.exception.AgendamentoConflictException;
import com.hospital.agendamento.messaging.MessagePublisher;
import com.hospital.agendamento.repository.ConsultaRepository;
import com.hospital.agendamento.repository.PacienteRepository;
import com.hospital.agendamento.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Serviço para gerenciamento de agendamento de consultas
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ConsultaService {

    private final ConsultaRepository consultaRepository;
    private final PacienteRepository pacienteRepository;
    private final MessagePublisher messagePublisher;
    private final WebClient.Builder webClientBuilder;

    @Value("${service.clinica.url}")
    private String clinicaServiceUrl;

    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Cadastra nova consulta
     */
    @Transactional
    public ResponseDTO cadastrarConsulta(ConsultaRequestDTO request) {
        log.info("Iniciando cadastro de consulta para paciente: {}", 
            request.getPaciente().getCpf());

        // 1. Validar e processar dados
        String cpf = request.getPaciente().getCpf();
        LocalDateTime horario = parseHorario(request.getHorario());

        // 2. Verificar se paciente já tem agendamento no mesmo horário
        if (consultaRepository.existsConsultaByPacienteAndHorario(cpf, horario)) {
            throw new AgendamentoConflictException(
                "O paciente já possui uma consulta agendada para este horário: " + 
                request.getHorario());
        }

        // 3. Salvar ou buscar paciente (com ownership)
        Paciente paciente = salvarOuBuscarPaciente(request.getPaciente());

        // 4. Verificar disponibilidade na clínica
        verificarDisponibilidadeClinica(horario, request.getMedico());

        // 5. Criar consulta local
        Consulta consulta = Consulta.builder()
            .cpfPaciente(cpf)
            .nomePaciente(request.getPaciente().getNome())
            .horario(horario)
            .especialidadeMedico(request.getMedico())
            .status(Consulta.StatusConsulta.AGENDADA)
            .createdBy(SecurityUtils.currentUsername())
            .build();

        consulta = Objects.requireNonNull(
            consultaRepository.save(consulta),
            "consultaRepository.save returned null"
        );
        Long consultaId = Objects.requireNonNull(
            consulta.getId(),
            "consulta.id must not be null after save"
        );
        log.info("Consulta salva localmente com ID: {}", consultaId);

        // 6. Enviar mensagem para a clínica via RabbitMQ
        MessagePublisher.ConsultaMessage message = MessagePublisher.ConsultaMessage.builder()
            .consultaId(consultaId)
            .cpfPaciente(cpf)
            .nomePaciente(paciente.getNome())
            .horario(horario)
            .especialidadeMedico(request.getMedico())
            .createdBy(SecurityUtils.currentUsername())
            .acao("CRIAR")
            .build();

        messagePublisher.publicarConsulta(message);

        // 7. Retornar resposta
        ResponseDTO response = ResponseDTO.builder()
            .mensagem(String.format(
                "O %s de %s foi marcado para %s",
                "medico",
                paciente.getNome(),
                request.getHorario()
            ))
            .codigo("Aguardando confirmação da clínica")
            .build();
        return response;
    }

    /**
     * Busca consultas por CPF do paciente
     */
    public List<Consulta> buscarConsultasPorCpf(String cpf) {
        log.info("Buscando consultas para CPF: {}", cpf);

        if (SecurityUtils.isPlainUsuario()) {
            Paciente paciente = pacienteRepository.findByCpf(cpf)
                .orElseThrow(() -> new AccessDeniedException("Paciente não encontrado para este CPF"));
            if (paciente.getCreatedBy() == null || !paciente.getCreatedBy().equals(SecurityUtils.currentUsername())) {
                throw new AccessDeniedException("USUARIO só pode consultar dados do próprio CPF");
            }
        }

        return consultaRepository.findByCpfPaciente(cpf);
    }

    /**
     * Busca consultas por nome do paciente
     */
    public List<Consulta> buscarConsultasPorNome(String nome) {
        log.info("Buscando consultas para nome: {}", nome);
        return consultaRepository.findByNomePacienteContainingIgnoreCase(nome);
    }

    /**
     * Atualiza ID da clínica na consulta (chamado após receber confirmação)
     */
    @Transactional
    public void atualizarIdClinica(Long consultaId, Long idClinica) {
        Long safeConsultaId = Objects.requireNonNull(consultaId, "consultaId must not be null");
        Long safeIdClinica = Objects.requireNonNull(idClinica, "idClinica must not be null");
        log.info("Atualizando consulta {} com ID da clínica: {}", safeConsultaId, safeIdClinica);
        Consulta consulta = consultaRepository.findById(safeConsultaId)
            .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));
        consulta.setIdClinica(safeIdClinica);
        consulta.setStatus(Consulta.StatusConsulta.CONFIRMADA);
        consultaRepository.save(consulta);
    }

    /**
     * Cancela uma consulta
     */
    @Transactional
    public void cancelarConsulta(Long consultaId) {
        Long safeConsultaId = Objects.requireNonNull(consultaId, "consultaId must not be null");
        log.info("Cancelando consulta: {}", safeConsultaId);
        Consulta consulta = consultaRepository.findById(safeConsultaId)
            .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));
        
        consulta.setStatus(Consulta.StatusConsulta.CANCELADA);
        consultaRepository.save(consulta);

        // Notificar clínica via mensageria
        MessagePublisher.ConsultaMessage message = MessagePublisher.ConsultaMessage.builder()
            .consultaId(safeConsultaId)
            .cpfPaciente(consulta.getCpfPaciente())
            .acao("CANCELAR")
            .build();
        
        messagePublisher.publicarConsulta(message);
    }

    /**
     * Verifica disponibilidade na clínica via HTTP
     */
    private void verificarDisponibilidadeClinica(LocalDateTime horario, String especialidade) {
        log.info("Verificando disponibilidade na clínica para: {} - {}", horario, especialidade);

        if (clinicaServiceUrl == null) {
            throw new IllegalStateException("Config service.clinica.url must be configured");
        }
        WebClient webClient = webClientBuilder.baseUrl(clinicaServiceUrl).build();
        
        Map<String, Object> requestBody = Map.of(
            "horario", horario.format(FORMATTER),
            "especialidade", especialidade
        );

        try {
            Boolean disponivel = webClient.post()
                .uri("/api/clinica/verificar-disponibilidade")
                .bodyValue((Object) requestBody)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();

            if (Boolean.FALSE.equals(disponivel)) {
                throw new AgendamentoConflictException(
                    "O " + especialidade + " já possui angendamento no " + 
                    horario.format(FORMATTER));
            }
        } catch (Exception e) {
            log.error("Erro ao verificar disponibilidade na clínica", e);
            // Continua o processo mesmo com erro de comunicação
            // Em produção, pode-se implementar circuit breaker
        }
    }

    /**
     * Salva ou busca paciente existente
     */
    private Paciente salvarOuBuscarPaciente(ConsultaRequestDTO.PacienteDTO pacienteDTO) {
        return pacienteRepository.findByCpf(pacienteDTO.getCpf())
            .map(existing -> {
                if (SecurityUtils.isPlainUsuario()) {
                    String owner = existing.getCreatedBy();
                    if (owner != null && !owner.equals(SecurityUtils.currentUsername())) {
                        throw new AccessDeniedException("USUARIO só pode cadastrar/usar o próprio CPF");
                    }
                }
                if (existing.getCreatedBy() == null) {
                    existing.setCreatedBy(SecurityUtils.currentUsername());
                    Paciente saved = pacienteRepository.save(existing);
                    if (saved == null) {
                        throw new IllegalStateException("pacienteRepository.save returned null");
                    }
                    return saved;
                }
                return existing;
            })
            .orElseGet(() -> {
                Paciente novoPaciente = Paciente.builder()
                    .nome(pacienteDTO.getNome())
                    .cpf(pacienteDTO.getCpf())
                    .idade(pacienteDTO.getIdade())
                    .sexo(pacienteDTO.getSexo())
                    .createdBy(SecurityUtils.currentUsername())
                    .build();
                Paciente saved = pacienteRepository.save(novoPaciente);
                if (saved == null) {
                    throw new IllegalStateException("pacienteRepository.save returned null");
                }
                return saved;
            });
    }

    /**
     * Faz parse do horário do formato texto para LocalDateTime
     */
    private LocalDateTime parseHorario(String horarioStr) {
        try {
            return LocalDateTime.parse(horarioStr, FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Formato de horário inválido. Use: dd/MM/yyyy HH:mm");
        }
    }
}
