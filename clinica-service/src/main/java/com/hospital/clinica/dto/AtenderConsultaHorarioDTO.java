package com.hospital.clinica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para atender consulta por horário
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtenderConsultaHorarioDTO {

    @NotBlank(message = "CPF do paciente é obrigatório")
    @JsonProperty("CPF paciente")
    private String cpfPaciente;

    @NotBlank(message = "Horário é obrigatório")
    @JsonProperty("Horario")
    private String horario;

    @NotNull(message = "Sintomas são obrigatórios")
    @JsonProperty("Sintomas")
    private List<String> sintomas;
}
