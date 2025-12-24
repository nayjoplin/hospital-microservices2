package com.hospital.clinica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExameSolicitadoDTO {
    @JsonProperty("tipoExame")
    private String tipoExame;

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("codigoExame")
    private String codigoExame;
}
