package com.hospital.centrocirurgico.dto;

public class DisponibilidadeDTO {
    private String horario;
    private String tipoExame;

    public DisponibilidadeDTO() {}

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getTipoExame() { return tipoExame; }
    public void setTipoExame(String tipoExame) { this.tipoExame = tipoExame; }
}
