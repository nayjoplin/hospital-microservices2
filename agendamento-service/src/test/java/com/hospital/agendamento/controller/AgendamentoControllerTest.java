package com.hospital.agendamento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.agendamento.dto.ConsultaRequestDTO;
import com.hospital.agendamento.dto.ResponseDTO;
import com.hospital.agendamento.service.ConsultaService;
import com.hospital.agendamento.service.ExameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AgendamentoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsultaService consultaService;

    @MockBean
    private ExameService exameService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testCadastrarConsulta() throws Exception {
        ConsultaRequestDTO request = ConsultaRequestDTO.builder()
            .paciente(ConsultaRequestDTO.PacienteDTO.builder()
                .nome("Paciente Teste")
                .cpf("123.456.789-00")
                .idade(30)
                .sexo("M")
                .build())
            .horario("24/12/2025 10:00")
            .medico("pediatra")
            .build();

        when(consultaService.cadastrarConsulta(any(ConsultaRequestDTO.class)))
                .thenReturn(new ResponseDTO("Consulta cadastrada com sucesso", "123"));

        mockMvc.perform(post("/api/cadastro/consulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
