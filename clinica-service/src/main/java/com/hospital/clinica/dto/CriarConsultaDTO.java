package com.hospital.clinica.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CriarConsultaDTO {
    private Long consultaId;
    private String cpfPaciente;
    private String nomePaciente;
    private String horario;
    private String especialidadeMedico;
    private String createdBy;
}
