package com.hospital.centrocirurgico.dto;

import com.hospital.centrocirurgico.entity.Procedimento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para atualização mínima de procedimento.
 */
public class AtualizarProcedimentoDTO {

    @Schema(description = "Novo horário do procedimento")
    private LocalDateTime horario;

    @Schema(description = "Novo status do procedimento", example = "CONCLUIDO")
    private Procedimento.StatusProcedimento status;

    @Schema(description = "Resultado do procedimento")
    private String resultado;

    public AtualizarProcedimentoDTO() {}

    public LocalDateTime getHorario() {
        return horario;
    }

    public void setHorario(LocalDateTime horario) {
        this.horario = horario;
    }

    public Procedimento.StatusProcedimento getStatus() {
        return status;
    }

    public void setStatus(Procedimento.StatusProcedimento status) {
        this.status = status;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}
