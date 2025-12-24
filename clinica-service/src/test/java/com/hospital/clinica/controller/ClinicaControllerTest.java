package com.hospital.clinica.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.clinica.dto.AtendimentoResponseDTO;
import com.hospital.clinica.service.ClinicaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClinicaController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClinicaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClinicaService clinicaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testAtenderConsultaPorHorario() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("CPF paciente", "123.456.789-00");
        request.put("Horario", "2025-12-25T10:00:00");
        request.put("Sintomas", Collections.singletonList("Dor de cabeça"));

        when(clinicaService.atenderConsultaPorHorario(any()))
                .thenReturn(new AtendimentoResponseDTO());

        mockMvc.perform(post("/api/clinica/AtenderConsulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    public void testAtenderConsultaPorCodigo() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("CPF paciente", "123.456.789-00");
        request.put("Codigo consulta", "C123");
        request.put("Sintomas", Collections.singletonList("Febre"));

        when(clinicaService.atenderConsultaPorCodigo(any()))
                .thenReturn(new AtendimentoResponseDTO());

        mockMvc.perform(post("/api/clinica/AtenderConsulta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
