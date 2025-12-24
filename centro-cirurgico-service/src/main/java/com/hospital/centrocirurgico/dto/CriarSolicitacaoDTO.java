package com.hospital.centrocirurgico.dto;

public class CriarSolicitacaoDTO {
    private String cpfPaciente;
    private String nomePaciente;
    private String tipoExame;
    private String prioridade;
    private String solicitadoPor;

    public CriarSolicitacaoDTO() {}

    public String getCpfPaciente() { return cpfPaciente; }
    public void setCpfPaciente(String cpfPaciente) { this.cpfPaciente = cpfPaciente; }

    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }

    public String getTipoExame() { return tipoExame; }
    public void setTipoExame(String tipoExame) { this.tipoExame = tipoExame; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public String getSolicitadoPor() { return solicitadoPor; }
    public void setSolicitadoPor(String solicitadoPor) { this.solicitadoPor = solicitadoPor; }
}
