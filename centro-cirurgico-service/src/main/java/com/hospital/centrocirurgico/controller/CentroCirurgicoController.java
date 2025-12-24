package com.hospital.centrocirurgico.controller;

import com.hospital.centrocirurgico.dto.*;
import com.hospital.centrocirurgico.entity.Procedimento;
import com.hospital.centrocirurgico.service.CentroCirurgicoService;
import com.hospital.centrocirurgico.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller do Centro Cirúrgico
 */
@RestController
@RequestMapping("/api/procedimentos")
@Tag(name = "Centro Cirúrgico", description = "Endpoints do centro cirúrgico")
@SecurityRequirement(name = "bearer-jwt")
public class CentroCirurgicoController {

    private final CentroCirurgicoService centroCirurgicoService;

    public CentroCirurgicoController(CentroCirurgicoService centroCirurgicoService) {
        this.centroCirurgicoService = centroCirurgicoService;
    }
        
    // Explicit logger to avoid Lombok/AP reliance
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CentroCirurgicoController.class);

    /**
     * Endpoint para marcar procedimento
     */
    @PostMapping("/marcar")
    @Operation(summary = "Marcar procedimento com horário")
    @PreAuthorize("hasAnyRole('USUARIO', 'MEDICO', 'ADMIN')")
    public ResponseEntity<ResponseDTO> marcarProcedimento(
            @Valid @RequestBody MarcarProcedimentoDTO dto) {
        
        log.info("Recebida requisição para marcar procedimento");
        try {
            ResponseDTO response = centroCirurgicoService.marcarProcedimento(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Horário já ocupado")) {
                return ResponseEntity.status(409).body(
                    ResponseDTO.builder()
                        .mensagem("Horário já ocupado para este tipo de procedimento")
                        .codigo("CONFLITO_HORARIO")
                        .build()
                );
            }
            throw e; // Re-throw other exceptions
        }
    }

    /**
     * Endpoint para criar solicitação de exame
     */
    @PostMapping("/criar-solicitacao")
    @Operation(summary = "Criar solicitação de exame")
    @PreAuthorize("hasAnyRole('MEDICO', 'ADMIN')")
    public ResponseEntity<ResponseDTO> criarSolicitacao(
            @Valid @RequestBody CriarSolicitacaoDTO dto) {
        
        log.info("Recebida solicitação de exame");
        ResponseDTO response = centroCirurgicoService.criarSolicitacao(dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar disponibilidade
     */
    @PostMapping("/verificar-disponibilidade")
    @Operation(summary = "Verificar disponibilidade de horário")
    public ResponseEntity<Boolean> verificarDisponibilidade(
            @RequestBody DisponibilidadeDTO dto) {
        
        log.info("Verificando disponibilidade");
        boolean disponivel = centroCirurgicoService.verificarDisponibilidade(
            dto.getHorario(), dto.getTipoExame());
        return ResponseEntity.ok(disponivel);
    }

    /**
     * Endpoint para buscar procedimentos por CPF
     */
    @GetMapping("/cpf/{cpf}")
    @Operation(summary = "Buscar procedimentos por CPF")
    @PreAuthorize("hasAnyRole('USUARIO', 'MEDICO', 'ADMIN')")
    public ResponseEntity<List<Procedimento>> buscarPorCpf(@PathVariable String cpf) {
        log.info("Buscando procedimentos para CPF: {}", cpf);
        List<Procedimento> procedimentos = centroCirurgicoService.buscarPorCpf(cpf);

        if (SecurityUtils.isPlainUsuario()) {
            String username = SecurityUtils.currentUsername();
            boolean ownsAny = procedimentos.stream().anyMatch(p -> username.equals(p.getCreatedBy()));
            boolean allOwnedOrNull = procedimentos.stream().allMatch(p -> p.getCreatedBy() == null || username.equals(p.getCreatedBy()));
            if (!ownsAny && !procedimentos.isEmpty()) {
                throw new AccessDeniedException("USUARIO só pode consultar dados do próprio CPF");
            }
            if (!allOwnedOrNull) {
                throw new AccessDeniedException("USUARIO só pode consultar dados do próprio CPF");
            }
        }

        return ResponseEntity.ok(procedimentos);
    }
}
