package com.hospital.agendamento.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * DTO para resposta padrão de sucesso
 */
@Builder
public class ResponseDTO {

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("codigo")
    private String codigo;

    public ResponseDTO() {
    }

    public ResponseDTO(String mensagem, String codigo) {
        this.mensagem = mensagem;
        this.codigo = codigo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}

/**
 * DTO para resposta de erro/conflito
 */
class ErrorResponseDTO {

    @JsonProperty("mensagem")
    private String mensagem;

    @JsonProperty("detalhes")
    private String detalhes;

    public ErrorResponseDTO() {
    }

    public ErrorResponseDTO(String mensagem, String detalhes) {
        this.mensagem = mensagem;
        this.detalhes = detalhes;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }
}

/**
 * DTO para verificação de disponibilidade
 */
class DisponibilidadeDTO {
    
    private String horario;
    private String tipoServico; // consulta ou exame
    private String especialidadeOuTipo;
    private boolean disponivel;

    public DisponibilidadeDTO() {
    }

    public DisponibilidadeDTO(String horario, String tipoServico, String especialidadeOuTipo, boolean disponivel) {
        this.horario = horario;
        this.tipoServico = tipoServico;
        this.especialidadeOuTipo = especialidadeOuTipo;
        this.disponivel = disponivel;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getTipoServico() {
        return tipoServico;
    }

    public void setTipoServico(String tipoServico) {
        this.tipoServico = tipoServico;
    }

    public String getEspecialidadeOuTipo() {
        return especialidadeOuTipo;
    }

    public void setEspecialidadeOuTipo(String especialidadeOuTipo) {
        this.especialidadeOuTipo = especialidadeOuTipo;
    }

    public boolean isDisponivel() {
        return disponivel;
    }

    public void setDisponivel(boolean disponivel) {
        this.disponivel = disponivel;
    }
}
