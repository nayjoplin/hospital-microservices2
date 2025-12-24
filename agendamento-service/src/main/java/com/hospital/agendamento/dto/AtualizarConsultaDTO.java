package com.hospital.agendamento.dto;

import com.hospital.agendamento.entity.Consulta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para atualização mínima de consulta.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarConsultaDTO {

    @Schema(description = "Novo status da consulta", example = "CANCELADA")
    private Consulta.StatusConsulta status;
}
