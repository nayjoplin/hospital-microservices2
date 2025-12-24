package com.hospital.agendamento.controller;

import com.hospital.agendamento.dto.AtualizarExameDTO;
import com.hospital.agendamento.dto.ExameRequestDTO;
import com.hospital.agendamento.dto.ResponseDTO;
import com.hospital.agendamento.entity.Exame;
import com.hospital.agendamento.repository.ExameRepository;
import com.hospital.agendamento.security.SecurityUtils;
import com.hospital.agendamento.service.ExameService;
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
@RequestMapping("/api/exames")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Exames", description = "CRUD mínimo de exames")
@SuppressWarnings("null")
public class ExameCrudController {

    private final ExameRepository exameRepository;
    private final ExameService exameService;

    @GetMapping
    @Operation(summary = "Listar exames")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<List<Exame>> listar() {
        return ResponseEntity.ok(exameRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar exame por ID")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<Exame> buscar(@PathVariable Long id) {
        return exameRepository.findById(id)
            .map(exame -> {
                enforceExameOwnership(exame);
                return ResponseEntity.ok(exame);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar exames por CPF")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<List<Exame>> buscarPorCpf(@PathVariable String cpf) {
        // Reusa regra de ownership existente no service
        return ResponseEntity.ok(exameService.buscarExamesPorCpf(cpf));
    }

    @PostMapping
    @Operation(summary = "Criar exame")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<ResponseDTO> criar(@Valid @RequestBody ExameRequestDTO request) {
        ResponseDTO response = exameService.cadastrarExame(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar exame (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Exame> atualizar(@PathVariable Long id, @RequestBody AtualizarExameDTO dto) {
        return exameRepository.findById(id)
            .map(existing -> {
                if (dto != null && dto.getStatus() != null) {
                    existing.setStatus(dto.getStatus());
                }
                Exame saved = exameRepository.save(existing);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar exame")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        Exame existing = exameRepository.findById(id).orElse(null);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        enforceExameOwnership(existing);
        exameService.cancelarExame(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void enforceExameOwnership(Exame exame) {
        if (!SecurityUtils.isPlainUsuario()) {
            return;
        }
        String owner = exame.getCreatedBy();
        if (owner == null || !owner.equals(SecurityUtils.currentUsername())) {
            throw new AccessDeniedException("USUARIO só pode acessar dados do próprio CPF");
        }
    }
}
