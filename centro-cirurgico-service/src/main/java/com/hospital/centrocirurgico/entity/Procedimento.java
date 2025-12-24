package com.hospital.centrocirurgico.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * Entidade que representa um Procedimento/Exame.
 */
@Entity
@Table(name = "procedimentos", indexes = {
    @Index(name = "idx_proc_horario", columnList = "horario"),
    @Index(name = "idx_proc_cpf", columnList = "cpf_paciente")
})
public class Procedimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_agendamento")
    private Long idAgendamento;

    @NotBlank(message = "CPF do paciente é obrigatório")
    @Column(name = "cpf_paciente", nullable = false, length = 14)
    private String cpfPaciente;

    @NotBlank(message = "Nome do paciente é obrigatório")
    @Column(name = "nome_paciente", nullable = false, length = 200)
    private String nomePaciente;

    /**
     * Identificador do usuário (Keycloak) que originou o agendamento.
     * Usado para restringir acesso de role USUARIO aos próprios dados.
     */
    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "horario")
    private LocalDateTime horario;

    @NotBlank(message = "Tipo de procedimento é obrigatório")
    @Column(name = "tipo_procedimento", nullable = false, length = 100)
    private String tipoProcedimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoProcedimento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PrioridadeProcedimento prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusProcedimento status;

    @Column(name = "solicitado_por", length = 200)
    private String solicitadoPor;

    @Column(name = "resultado", columnDefinition = "TEXT")
    private String resultado;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao")
    private LocalDateTime dataAtualizacao;

    @Column(name = "data_realizacao")
    private LocalDateTime dataRealizacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (status == null) {
            status = StatusProcedimento.AGUARDANDO_AGENDAMENTO;
        }
        if (prioridade == null) {
            prioridade = PrioridadeProcedimento.PADRAO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Procedimento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdAgendamento() { return idAgendamento; }
    public void setIdAgendamento(Long idAgendamento) { this.idAgendamento = idAgendamento; }

    public String getCpfPaciente() { return cpfPaciente; }
    public void setCpfPaciente(String cpfPaciente) { this.cpfPaciente = cpfPaciente; }

    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getHorario() { return horario; }
    public void setHorario(LocalDateTime horario) { this.horario = horario; }

    public String getTipoProcedimento() { return tipoProcedimento; }
    public void setTipoProcedimento(String tipoProcedimento) { this.tipoProcedimento = tipoProcedimento; }

    public TipoProcedimento getTipo() { return tipo; }
    public void setTipo(TipoProcedimento tipo) { this.tipo = tipo; }

    public PrioridadeProcedimento getPrioridade() { return prioridade; }
    public void setPrioridade(PrioridadeProcedimento prioridade) { this.prioridade = prioridade; }

    public StatusProcedimento getStatus() { return status; }
    public void setStatus(StatusProcedimento status) { this.status = status; }

    public String getSolicitadoPor() { return solicitadoPor; }
    public void setSolicitadoPor(String solicitadoPor) { this.solicitadoPor = solicitadoPor; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public LocalDateTime getDataRealizacao() { return dataRealizacao; }
    public void setDataRealizacao(LocalDateTime dataRealizacao) { this.dataRealizacao = dataRealizacao; }

    public enum TipoProcedimento {
        EXAME_SIMPLES,
        EXAME_ALTA_COMPLEXIDADE,
        CIRURGIA,
        PROCEDIMENTO_AMBULATORIAL
    }

    public enum PrioridadeProcedimento {
        BAIXA,
        PADRAO,
        ALTA,
        EMERGENCIAL
    }

    public enum StatusProcedimento {
        AGUARDANDO_AGENDAMENTO,
        AGENDADO,
        EM_PREPARACAO,
        EM_ANDAMENTO,
        CONCLUIDO,
        CANCELADO
    }
}

