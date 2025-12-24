package com.hospital.clinica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtenderConsultaCodigoDTO {
    @NotBlank(message = "CPF do paciente é obrigatório")
    @JsonProperty("CPF paciente")
    private String cpfPaciente;

    @NotBlank(message = "Código da consulta é obrigatório")
    @JsonProperty("Codigo consulta")
    private String codigoConsulta;

    @NotNull(message = "Sintomas são obrigatórios")
    @JsonProperty("Sintomas")
    private List<String> sintomas;
}
