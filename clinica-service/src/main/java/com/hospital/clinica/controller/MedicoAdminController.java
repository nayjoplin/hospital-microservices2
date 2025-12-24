package com.hospital.clinica.controller;

import com.hospital.clinica.entity.Medico;
import com.hospital.clinica.repository.MedicoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/medicos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Admin - Médicos", description = "CRUD administrativo de médicos")
public class MedicoAdminController {

    private final MedicoRepository medicoRepository;

    @PostMapping
    @Operation(summary = "Cadastrar médico (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Medico> criar(@Valid @RequestBody Medico medico) {
        if (medico.getId() != null) {
            medico.setId(null);
        }
        Medico saved = medicoRepository.save(medico);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar médico (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Medico> atualizar(@PathVariable Long id, @Valid @RequestBody Medico medico) {
        return medicoRepository.findById(id)
            .map(existing -> {
                medico.setId(existing.getId());
                Medico saved = medicoRepository.save(medico);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover médico (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!medicoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        medicoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
