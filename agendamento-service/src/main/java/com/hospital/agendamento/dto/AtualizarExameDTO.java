package com.hospital.agendamento.dto;

import com.hospital.agendamento.entity.Exame;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para atualização mínima de exame.
 */
public class AtualizarExameDTO {

    @Schema(description = "Novo status do exame", example = "CANCELADO")
    private Exame.StatusExame status;

    public AtualizarExameDTO() {
    }

    public AtualizarExameDTO(Exame.StatusExame status) {
        this.status = status;
    }

    public Exame.StatusExame getStatus() {
        return status;
    }

    public void setStatus(Exame.StatusExame status) {
        this.status = status;
    }
}
