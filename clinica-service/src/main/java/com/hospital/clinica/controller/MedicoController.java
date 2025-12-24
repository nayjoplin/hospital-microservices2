package com.hospital.clinica.controller;

import com.hospital.clinica.entity.Medico;
import com.hospital.clinica.repository.MedicoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinica/medicos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Médicos", description = "Consulta de médicos")
public class MedicoController {

    private final MedicoRepository medicoRepository;

    @GetMapping
    @Operation(summary = "Listar médicos")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<List<Medico>> listar() {
        return ResponseEntity.ok(medicoRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar médico por ID")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<Medico> buscar(@PathVariable long id) {
        return medicoRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
