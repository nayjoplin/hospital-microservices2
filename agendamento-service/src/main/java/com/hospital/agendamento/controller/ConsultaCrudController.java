package com.hospital.agendamento.controller;

import com.hospital.agendamento.dto.AtualizarConsultaDTO;
import com.hospital.agendamento.dto.ConsultaRequestDTO;
import com.hospital.agendamento.dto.ResponseDTO;
import com.hospital.agendamento.entity.Consulta;
import com.hospital.agendamento.repository.ConsultaRepository;
import com.hospital.agendamento.security.SecurityUtils;
import com.hospital.agendamento.service.ConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Consultas", description = "CRUD mínimo de consultas")
@SuppressWarnings("null")
public class ConsultaCrudController {

    private final ConsultaRepository consultaRepository;
    private final ConsultaService consultaService;

    @GetMapping
    @Operation(summary = "Listar consultas")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<List<Consulta>> listar() {
        return ResponseEntity.ok(consultaRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar consulta por ID")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<Consulta> buscar(@PathVariable Long id) {
        return consultaRepository.findById(id)
            .map(consulta -> {
                enforceConsultaOwnership(consulta);
                return ResponseEntity.ok(consulta);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar consultas por CPF")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<List<Consulta>> buscarPorCpf(@PathVariable String cpf) {
        // Reusa regra de ownership existente no service
        return ResponseEntity.ok(consultaService.buscarConsultasPorCpf(cpf));
    }

    @PostMapping
    @Operation(summary = "Criar consulta")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<ResponseDTO> criar(@Valid @RequestBody ConsultaRequestDTO request) {
        ResponseDTO response = consultaService.cadastrarConsulta(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar consulta (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Consulta> atualizar(@PathVariable Long id, @RequestBody AtualizarConsultaDTO dto) {
        return consultaRepository.findById(id)
            .map(existing -> {
                if (dto != null && dto.getStatus() != null) {
                    existing.setStatus(dto.getStatus());
                }
                Consulta saved = consultaRepository.save(existing);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar consulta")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        Consulta existing = consultaRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        enforceConsultaOwnership(existing);
        consultaService.cancelarConsulta(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void enforceConsultaOwnership(Consulta consulta) {
        if (!SecurityUtils.isPlainUsuario()) {
            return;
        }
        String owner = consulta.getCreatedBy();
        if (owner == null || !owner.equals(SecurityUtils.currentUsername())) {
            throw new AccessDeniedException("USUARIO só pode acessar dados do próprio CPF");
        }
    }
}
