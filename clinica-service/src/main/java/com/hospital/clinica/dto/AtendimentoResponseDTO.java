package com.hospital.clinica.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoResponseDTO {
    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("possiveisDiagnosticos")
    private List<String> possiveisDiagnosticos;

    @JsonProperty("tratamentoSugerido")
    private String tratamentoSugerido;

    @JsonProperty("exameSolicitado")
    private ExameSolicitadoDTO exameSolicitado;

    @JsonProperty("prioridade")
    private String prioridade;

    @JsonProperty("preDiagnostico")
    private String preDiagnostico;
}
