package com.hospital.centrocirurgico.dto;

public class CriarExameDTO {
    private Long exameId;
    private String cpfPaciente;
    private String nomePaciente;
    private String horario;
    private String tipoExame;
    private String createdBy;

    public CriarExameDTO() {}

    public Long getExameId() { return exameId; }
    public void setExameId(Long exameId) { this.exameId = exameId; }

    public String getCpfPaciente() { return cpfPaciente; }
    public void setCpfPaciente(String cpfPaciente) { this.cpfPaciente = cpfPaciente; }

    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public String getTipoExame() { return tipoExame; }
    public void setTipoExame(String tipoExame) { this.tipoExame = tipoExame; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
