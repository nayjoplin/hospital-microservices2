package com.hospital.centrocirurgico.controller;

import com.hospital.centrocirurgico.dto.AtualizarProcedimentoDTO;
import com.hospital.centrocirurgico.entity.Procedimento;
import com.hospital.centrocirurgico.repository.ProcedimentoRepository;
import com.hospital.centrocirurgico.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/procedimentos")
@SecurityRequirement(name = "bearer-jwt")
@Tag(name = "Procedimentos", description = "CRUD mínimo de procedimentos")
@SuppressWarnings("null")
public class ProcedimentoCrudController {

    private final ProcedimentoRepository procedimentoRepository;

    public ProcedimentoCrudController(ProcedimentoRepository procedimentoRepository) {
        this.procedimentoRepository = procedimentoRepository;
    }

    @GetMapping
    @Operation(summary = "Listar procedimentos")
    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    public ResponseEntity<List<Procedimento>> listar() {
        return ResponseEntity.ok(procedimentoRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar procedimento por ID")
    @PreAuthorize("hasAnyRole('USUARIO','MEDICO','ADMIN')")
    public ResponseEntity<Procedimento> buscar(@PathVariable Long id) {
        return procedimentoRepository.findById(id)
            .map(proc -> {
                enforceOwnership(proc);
                return ResponseEntity.ok(proc);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar procedimento (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Procedimento> criar(@Valid @RequestBody Procedimento procedimento) {
        if (procedimento.getId() != null) {
            procedimento.setId(null);
        }
        if (procedimento.getCreatedBy() == null || procedimento.getCreatedBy().isBlank()) {
            procedimento.setCreatedBy(SecurityUtils.currentUsername());
        }
        Procedimento saved = procedimentoRepository.save(procedimento);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar procedimento (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Procedimento> atualizar(@PathVariable Long id, @RequestBody AtualizarProcedimentoDTO dto) {
        return procedimentoRepository.findById(id)
            .map(existing -> {
                if (dto != null) {
                    if (dto.getHorario() != null) {
                        existing.setHorario(dto.getHorario());
                    }
                    if (dto.getStatus() != null) {
                        existing.setStatus(dto.getStatus());
                    }
                    if (dto.getResultado() != null) {
                        existing.setResultado(dto.getResultado());
                    }
                }
                Procedimento saved = procedimentoRepository.save(existing);
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover procedimento (ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!procedimentoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        procedimentoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void enforceOwnership(Procedimento procedimento) {
        if (!SecurityUtils.isPlainUsuario()) {
            return;
        }
        String owner = procedimento.getCreatedBy();
        if (owner == null || !owner.equals(SecurityUtils.currentUsername())) {
            throw new AccessDeniedException("USUARIO só pode acessar dados do próprio CPF");
        }
    }
}
