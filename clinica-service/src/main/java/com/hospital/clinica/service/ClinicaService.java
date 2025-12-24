package com.hospital.clinica.service;

import com.hospital.clinica.dto.*;
import com.hospital.clinica.entity.ConsultaClinica;
import com.hospital.clinica.entity.Medico;
import com.hospital.clinica.entity.Sintoma;
import com.hospital.clinica.repository.ConsultaClinicaRepository;
import com.hospital.clinica.repository.MedicoRepository;
import com.hospital.clinica.repository.SintomaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Serviço principal da Clínica
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClinicaService {

    private final ConsultaClinicaRepository consultaRepository;
    private final MedicoRepository medicoRepository;
    private final SintomaRepository sintomaRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${service.centro-cirurgico.url:http://localhost:8083}")
    private String centroCirurgicoUrl;

    private static final DateTimeFormatter FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Cria consulta a partir de mensagem do RabbitMQ
     */
    @Transactional
    public Long criarConsulta(CriarConsultaDTO dto) {
        log.info("Criando consulta para paciente: {}", dto.getCpfPaciente());

        LocalDateTime horario = LocalDateTime.parse(dto.getHorario(), FORMATTER);

        // Buscar médico pela especialidade
        List<Medico> medicos = medicoRepository.findByEspecialidade(dto.getEspecialidadeMedico());
        Medico medico = medicos.isEmpty() ? null : medicos.get(0);

        ConsultaClinica consulta = ConsultaClinica.builder()
            .idAgendamento(dto.getConsultaId())
            .cpfPaciente(dto.getCpfPaciente())
            .nomePaciente(dto.getNomePaciente())
            .createdBy(dto.getCreatedBy())
            .horario(horario)
            .especialidadeMedico(dto.getEspecialidadeMedico())
            .medico(medico)
            .status(ConsultaClinica.StatusConsulta.AGENDADA)
            .build();

        consulta = consultaRepository.save(consulta);
        log.info("Consulta criada com ID: {}", consulta.getId());

        return consulta.getId();
    }

    /**
     * Verifica disponibilidade de horário
     */
    public boolean verificarDisponibilidade(String horario, String especialidade) {
        log.info("Verificando disponibilidade: {} - {}", horario, especialidade);

        try {
            LocalDateTime dateTime = LocalDateTime.parse(horario, FORMATTER);
            Optional<ConsultaClinica> consulta = consultaRepository
                .findByEspecialidadeAndHorario(especialidade, dateTime);
            
            boolean disponivel = consulta.isEmpty();
            log.info("Disponibilidade: {}", disponivel);
            return disponivel;
        } catch (Exception e) {
            log.error("Erro ao verificar disponibilidade", e);
            return false;
        }
    }

    /**
     * Atende consulta por horário
     */
    @Transactional
    public AtendimentoResponseDTO atenderConsultaPorHorario(AtenderConsultaHorarioDTO dto) {
        log.info("Atendendo consulta por horário: {}", dto);

        LocalDateTime horario = LocalDateTime.parse(dto.getHorario(), FORMATTER);

        // Buscar consulta
        ConsultaClinica consulta = consultaRepository
            .findByCpfAndHorario(dto.getCpfPaciente(), horario)
            .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));

        return processarAtendimento(consulta, dto.getSintomas());
    }

    /**
     * Atende consulta por código
     */
    @Transactional
    public AtendimentoResponseDTO atenderConsultaPorCodigo(AtenderConsultaCodigoDTO dto) {
        log.info("Atendendo consulta por código: {}", dto);

        Long codigoConsulta = Long.parseLong(dto.getCodigoConsulta());

        // Buscar consulta
        ConsultaClinica consulta = consultaRepository.findById(codigoConsulta)
            .orElseThrow(() -> new RuntimeException("Consulta não encontrada"));

        // Verificar se CPF bate
        if (!consulta.getCpfPaciente().equals(dto.getCpfPaciente())) {
            throw new RuntimeException("CPF não corresponde à consulta");
        }

        return processarAtendimento(consulta, dto.getSintomas());
    }

    /**
     * Processa o atendimento da consulta
     */
    private AtendimentoResponseDTO processarAtendimento(
            ConsultaClinica consulta, List<String> sintomasNomes) {
        
        log.info("Processando atendimento para consulta ID: {}", consulta.getId());

        // Atualizar status
        consulta.setStatus(ConsultaClinica.StatusConsulta.EM_ATENDIMENTO);
        consulta.setDataAtendimento(LocalDateTime.now());
        consulta.setSintomas(sintomasNomes);

        // Buscar sintomas no banco
        List<String> sintomasLower = sintomasNomes.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        
        List<Sintoma> sintomas = sintomaRepository.findByNomesIn(sintomasLower);

        // Calcular prioridade segundo protocolo Manchester
        // 1) regras imediatas (sinais de emergência)
        boolean temConvulsao = sintomasLower.stream().anyMatch(s -> s.contains("convuls"));
        boolean temPerdaConsciencia = sintomasLower.stream().anyMatch(s -> s.contains("perda_consciencia") || s.contains("perda de consciencia"));
        boolean temDorPeito = sintomasLower.stream().anyMatch(s -> s.contains("dor_peito") || s.contains("dor no peito") || s.contains("dor_peito_intensa"));
        boolean temDificuldadeRespirar = sintomasLower.stream().anyMatch(s -> s.contains("dificuldade_respirar") || s.contains("dificuldade para respirar") || s.contains("dispneia"));
        boolean temTraumaCraniano = sintomasLower.stream().anyMatch(s -> s.contains("trauma_craniano") || s.contains("trauma"));
        boolean temVomito = sintomasLower.stream().anyMatch(s -> s.contains("vomito") || s.contains("vômito"));
        boolean temConfusao = sintomasLower.stream().anyMatch(s -> s.contains("confusao") || s.contains("confusão"));

        ConsultaClinica.PrioridadeManchester prioridadeManchester = ConsultaClinica.PrioridadeManchester.AZUL;

        // emergency immediate rules
        if (temConvulsao || temPerdaConsciencia || temDorPeito || temDificuldadeRespirar) {
            prioridadeManchester = ConsultaClinica.PrioridadeManchester.VERMELHO;
        } else if (temTraumaCraniano && (temVomito || temConfusao || temConvulsao)) {
            prioridadeManchester = ConsultaClinica.PrioridadeManchester.VERMELHO;
        } else {
            // if no immediate emergency, calculate weighted score
            int score = 0;
            for (String s : sintomasLower) {
                Optional<Sintoma> opt = sintomaRepository.findByNomeIgnoreCase(s);
                if (opt.isPresent()) {
                    Sintoma.Prioridade p = opt.get().getPrioridade();
                    switch (p) {
                        case EMERGENCIAL: score += 10; break;
                        case ALTA: score += 8; break;
                        case PADRAO: score += 4; break;
                        case BAIXA: score += 1; break;
                    }
                } else {
                    // sintoma desconhecido, peso baixo
                    score += 1;
                }
            }

            if (score >= 15) {
                prioridadeManchester = ConsultaClinica.PrioridadeManchester.LARANJA;
            } else if (score >= 8) {
                prioridadeManchester = ConsultaClinica.PrioridadeManchester.AMARELO;
            } else if (score >= 4) {
                prioridadeManchester = ConsultaClinica.PrioridadeManchester.VERDE;
            } else {
                prioridadeManchester = ConsultaClinica.PrioridadeManchester.AZUL;
            }
        }

        // definir pre-diagnóstico simples baseado em regras
        String preDiagnostico = "Pré-diagnóstico: requer avaliação";
        if (prioridadeManchester == ConsultaClinica.PrioridadeManchester.VERMELHO) {
            preDiagnostico = "Suspeita de risco vital - encaminhar imediatamente";
        } else if (prioridadeManchester == ConsultaClinica.PrioridadeManchester.LARANJA) {
            preDiagnostico = "Quadro muito urgente - atendimento prioritário";
        } else if (prioridadeManchester == ConsultaClinica.PrioridadeManchester.AMARELO) {
            preDiagnostico = "Quadro urgente - requer investigação rápida";
        } else if (prioridadeManchester == ConsultaClinica.PrioridadeManchester.VERDE) {
            preDiagnostico = "Caso pouco urgente";
        } else {
            preDiagnostico = "Caso não urgente";
        }

        // também manter compatibilidade com lógica anterior (prioridade por sintoma)
        Sintoma.Prioridade prioridadeMax = sintomas.stream()
            .map(Sintoma::getPrioridade)
            .max(Comparator.comparing(Sintoma.Prioridade::getNivel))
            .orElse(Sintoma.Prioridade.PADRAO);

        // Gerar diagnóstico com base nos sintomas
        List<String> diagnosticos = gerarDiagnosticos(sintomasNomes);
        String diagnosticoTexto = String.join(", ", diagnosticos);
        consulta.setDiagnostico(diagnosticoTexto);

        // definir pre-diagnóstico e prioridade Manchester em consulta
        consulta.setPreDiagnostico(preDiagnostico);
        consulta.setPrioridadeManchester(prioridadeManchester);

        // Gerar tratamento sugerido
        String tratamento = gerarTratamento(sintomasNomes, prioridadeMax);
        consulta.setTratamentoSugerido(tratamento);

        // Verificar se precisa de exame de alta complexidade
        ExameSolicitadoDTO exameSolicitado = null;
        if (precisaExameAltaComplexidade(sintomasNomes, prioridadeMax)) {
            exameSolicitado = solicitarExameAltaComplexidade(consulta, sintomasNomes);
        }

        // Atualizar status para atendida
        consulta.setStatus(ConsultaClinica.StatusConsulta.ATENDIDA);
        consultaRepository.save(consulta);

        return AtendimentoResponseDTO.builder()
            .mensagem("Consulta atendida com sucesso")
            .possiveisDiagnosticos(diagnosticos)
            .tratamentoSugerido(tratamento)
            .exameSolicitado(exameSolicitado)
            .prioridade(prioridadeManchester.name())
            .preDiagnostico(preDiagnostico)
            .build();
    }

    /**
     * Gera diagnósticos baseados nos sintomas
     */
    private List<String> gerarDiagnosticos(List<String> sintomas) {
        Set<String> diagnosticos = new HashSet<>();

        // Regras de diagnóstico baseadas em sintomas
        List<String> sintomasLower = sintomas.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        // COVID, Gripe
        if (sintomasLower.containsAll(Arrays.asList("tosse", "febre", "cansaço")) ||
            sintomasLower.containsAll(Arrays.asList("tosse", "febre"))) {
            diagnosticos.add("COVID-19");
            diagnosticos.add("Gripe");
        }

        // Problemas musculoesqueléticos
        if (sintomasLower.contains("dores na perna") || 
            sintomasLower.contains("dor na perna")) {
            diagnosticos.add("Inchaço muscular");
            diagnosticos.add("Possível fratura");
        }

        // Enxaqueca
        if (sintomasLower.contains("dor de cabeça") || 
            sintomasLower.contains("cefaleia")) {
            diagnosticos.add("Enxaqueca");
            diagnosticos.add("Cefaleia tensional");
        }

        // Emergências
        if (sintomasLower.contains("sangramento agudo") || 
            sintomasLower.contains("dores internas")) {
            diagnosticos.add("Hemorragia interna");
            diagnosticos.add("Possível trauma interno");
        }

        // Náusea e vômito
        if (sintomasLower.contains("nausea") || sintomasLower.contains("náusea")) {
            diagnosticos.add("Gastroenterite");
            diagnosticos.add("Intoxicação alimentar");
        }

        if (diagnosticos.isEmpty()) {
            diagnosticos.add("Diagnóstico requer mais investigação");
        }

        return new ArrayList<>(diagnosticos);
    }

    /**
     * Gera tratamento sugerido
     */
    private String gerarTratamento(List<String> sintomas, Sintoma.Prioridade prioridade) {
        StringBuilder tratamento = new StringBuilder();

        if (prioridade == Sintoma.Prioridade.EMERGENCIAL) {
            tratamento.append("URGENTE: Encaminhar para emergência imediatamente. ");
        }

        tratamento.append("Tratamento sugerido: ");

        List<String> sintomasLower = sintomas.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        if (sintomasLower.contains("febre")) {
            tratamento.append("Antitérmico (Paracetamol 500mg 6/6h), ");
        }

        if (sintomasLower.contains("tosse")) {
            tratamento.append("Xarope expectorante, ");
        }

        if (sintomasLower.contains("dor de cabeça")) {
            tratamento.append("Analgésico (Dipirona 500mg 8/8h), ");
        }

        if (sintomasLower.contains("nausea") || sintomasLower.contains("náusea")) {
            tratamento.append("Antiemético (Metoclopramida 10mg), ");
        }

        tratamento.append("Repouso e hidratação adequada.");

        return tratamento.toString();
    }

    /**
     * Verifica se precisa de exame de alta complexidade
     */
    private boolean precisaExameAltaComplexidade(
            List<String> sintomas, Sintoma.Prioridade prioridade) {
        
        if (prioridade == Sintoma.Prioridade.EMERGENCIAL || 
            prioridade == Sintoma.Prioridade.ALTA) {
            return true;
        }

        List<String> sintomasLower = sintomas.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        // Casos que requerem exames de imagem
        return sintomasLower.contains("sangramento agudo") ||
               sintomasLower.contains("dores internas") ||
               sintomasLower.contains("fratura") ||
               sintomasLower.contains("trauma");
    }

    /**
     * Solicita exame de alta complexidade
     */
    private ExameSolicitadoDTO solicitarExameAltaComplexidade(
            ConsultaClinica consulta, List<String> sintomas) {
        
        log.info("Solicitando exame de alta complexidade para consulta: {}", 
            consulta.getId());

        // Determinar tipo de exame
        String tipoExame = determinarTipoExame(sintomas);

        // Criar solicitação para o centro cirúrgico
        Map<String, Object> solicitacao = Map.of(
            "cpfPaciente", consulta.getCpfPaciente(),
            "nomePaciente", consulta.getNomePaciente(),
            "tipoExame", tipoExame,
            "prioridade", "ALTA",
            "solicitadoPor", "Clínica - Consulta " + consulta.getId()
        );

        try {
            WebClient webClient = webClientBuilder.baseUrl(centroCirurgicoUrl).build();

            Map response = webClient.post()
                .uri("/api/procedimentos/criar-solicitacao")
                .body(Mono.just(solicitacao), Map.class)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null && response.containsKey("codigoExame")) {
                String codigoExame = response.get("codigoExame").toString();
                consulta.setExameSolicitadoId(Long.parseLong(codigoExame));

                return ExameSolicitadoDTO.builder()
                    .tipoExame(tipoExame)
                    .codigoExame(codigoExame)
                    .mensagem("Exame registrado sobre o CPF: " + consulta.getCpfPaciente() + 
                             " por favor agendar o horário em nosso sistema")
                    .build();
            }
        } catch (Exception e) {
            log.error("Erro ao solicitar exame de alta complexidade", e);
        }

        return null;
    }

    /**
     * Determina o tipo de exame necessário
     */
    private String determinarTipoExame(List<String> sintomas) {
        List<String> sintomasLower = sintomas.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());

        if (sintomasLower.contains("sangramento agudo") || 
            sintomasLower.contains("dores internas")) {
            return "Tomografia computadorizada";
        }

        if (sintomasLower.contains("dor na perna") || 
            sintomasLower.contains("fratura")) {
            return "Ressonância magnética";
        }

        if (sintomasLower.contains("dor de cabeça") && 
            sintomasLower.contains("forte")) {
            return "Tomografia de crânio";
        }

        return "Ressonância magnética";
    }
}
