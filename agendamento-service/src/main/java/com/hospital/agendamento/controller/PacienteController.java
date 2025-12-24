package com.hospital.agendamento.controller;

import com.hospital.agendamento.entity.Paciente;
import com.hospital.agendamento.exception.AgendamentoConflictException;
import com.hospital.agendamento.repository.PacienteRepository;
import com.hospital.agendamento.security.SecurityUtils;
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
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Pacientes", description = "CRUD mínimo de pacientes")
public class PacienteController {

    private final PacienteRepository pacienteRepository;

    @GetMapping
    @Operation(summary = "Listar pacientes")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<List<Paciente>> listar() {
        return ResponseEntity.ok(pacienteRepository.findAll());
    }

    @GetMapping("/{cpf}")
    @Operation(summary = "Buscar paciente por CPF")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<Paciente> buscarPorCpf(@PathVariable String cpf) {
        return pacienteRepository.findByCpf(cpf)
            .map(paciente -> {
                enforcePacienteOwnership(paciente);
                return ResponseEntity.ok(paciente);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar paciente")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<Paciente> criar(@Valid @RequestBody Paciente paciente) {
        if (paciente.getId() != null) {
            paciente.setId(null);
        }
        if (paciente.getCpf() == null || paciente.getCpf().isBlank()) {
            throw new AgendamentoConflictException("CPF é obrigatório");
        }

        if (pacienteRepository.existsByCpf(paciente.getCpf())) {
            Paciente existing = pacienteRepository.findByCpf(paciente.getCpf()).orElse(null);
            if (existing != null && SecurityUtils.isPlainUsuario()) {
                String owner = existing.getCreatedBy();
                if (owner != null && !owner.equals(SecurityUtils.currentUsername())) {
                    throw new AccessDeniedException("USUARIO só pode cadastrar/usar o próprio CPF");
                }
            }
            throw new AgendamentoConflictException("Já existe paciente cadastrado com este CPF");
        }

        // Nunca confiar no createdBy vindo do cliente
        if (SecurityUtils.isPlainUsuario() || paciente.getCreatedBy() == null || paciente.getCreatedBy().isBlank()) {
            paciente.setCreatedBy(SecurityUtils.currentUsername());
        }

        Paciente saved = pacienteRepository.save(paciente);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{cpf}")
    @Operation(summary = "Atualizar paciente por CPF")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    public ResponseEntity<Paciente> atualizar(@PathVariable String cpf, @Valid @RequestBody Paciente paciente) {
        return pacienteRepository.findByCpf(cpf)
            .map(existing -> {
                enforcePacienteOwnership(existing);

                // Mantém o CPF estável (rota manda)
                paciente.setId(existing.getId());
                paciente.setCpf(existing.getCpf());

                // Mantém ownership estável
                paciente.setCreatedBy(existing.getCreatedBy());

                Paciente saved = pacienteRepository.save(paciente);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{cpf}")
    @Operation(summary = "Remover paciente por CPF")
    @PreAuthorize("hasAnyRole('USUARIO','ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable String cpf) {
        Paciente existing = pacienteRepository.findByCpf(cpf).orElse(null);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        enforcePacienteOwnership(existing);
        pacienteRepository.delete(existing);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void enforcePacienteOwnership(Paciente paciente) {
        if (!SecurityUtils.isPlainUsuario()) {
            return;
        }
        String owner = paciente.getCreatedBy();
        if (owner == null || !owner.equals(SecurityUtils.currentUsername())) {
            throw new AccessDeniedException("USUARIO só pode acessar dados do próprio CPF");
        }
    }
}
