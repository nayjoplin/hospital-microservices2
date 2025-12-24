package com.hospital.centrocirurgico.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class MarcarProcedimentoDTO {

    @NotBlank(message = "CPF do paciente é obrigatório")
    @JsonProperty("CPF paciente")
    private String cpfPaciente;

    @NotBlank(message = "Código do exame é obrigatório")
    @JsonProperty("Codigo exame")
    private String codigoExame;

    @NotBlank(message = "Horário é obrigatório")
    @JsonProperty("Horario desejado")
    private String horarioDesejado;

    public MarcarProcedimentoDTO() {}

    public String getCpfPaciente() { return cpfPaciente; }
    public void setCpfPaciente(String cpfPaciente) { this.cpfPaciente = cpfPaciente; }

    public String getCodigoExame() { return codigoExame; }
    public void setCodigoExame(String codigoExame) { this.codigoExame = codigoExame; }

    public String getHorarioDesejado() { return horarioDesejado; }
    public void setHorarioDesejado(String horarioDesejado) { this.horarioDesejado = horarioDesejado; }
}
