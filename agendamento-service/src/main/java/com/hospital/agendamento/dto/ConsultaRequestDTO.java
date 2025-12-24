package com.hospital.agendamento.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO para requisição de cadastro de consulta
 */
public class ConsultaRequestDTO {

    @Valid
    @NotNull(message = "Dados do paciente são obrigatórios")
    @JsonProperty("Paciente")
    private PacienteDTO paciente;

    @NotBlank(message = "Horário é obrigatório")
    @JsonProperty("Horario")
    private String horario; // Formato: "dia e hora"

    @NotBlank(message = "Especialidade do médico é obrigatória")
    @JsonProperty("Medico")
    private String medico; // Ex: "Especialidade do medico. Ex: pediatra"

    public ConsultaRequestDTO() {
    }

    @Builder
    public ConsultaRequestDTO(PacienteDTO paciente, String horario, String medico) {
        this.paciente = paciente;
        this.horario = horario;
        this.medico = medico;
    }

    public PacienteDTO getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteDTO paciente) {
        this.paciente = paciente;
    }

    public String getHorario() {
        return horario;
    }

    public void setHorario(String horario) {
        this.horario = horario;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    /**
     * DTO interno para dados do paciente
     */
    public static class PacienteDTO {
        
        @NotBlank(message = "Nome é obrigatório")
        @JsonProperty("Nome")
        private String nome;

        @NotBlank(message = "CPF é obrigatório")
        @JsonProperty("CPF")
        private String cpf;

        @NotNull(message = "Idade é obrigatória")
        @JsonProperty("idade")
        private Integer idade;

        @NotBlank(message = "Sexo é obrigatório")
        @JsonProperty("Sexo")
        private String sexo;

        public PacienteDTO() {
        }

        @Builder
        public PacienteDTO(String nome, String cpf, Integer idade, String sexo) {
            this.nome = nome;
            this.cpf = cpf;
            this.idade = idade;
            this.sexo = sexo;
        }

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getCpf() {
            return cpf;
        }

        public void setCpf(String cpf) {
            this.cpf = cpf;
        }

        public Integer getIdade() {
            return idade;
        }

        public void setIdade(Integer idade) {
            this.idade = idade;
        }

        public String getSexo() {
            return sexo;
        }

        public void setSexo(String sexo) {
            this.sexo = sexo;
        }
    }
}
