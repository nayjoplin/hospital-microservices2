package com.hospital.centrocirurgico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.centrocirurgico.dto.MarcarProcedimentoDTO;
import com.hospital.centrocirurgico.dto.ResponseDTO;
import com.hospital.centrocirurgico.service.CentroCirurgicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CentroCirurgicoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CentroCirurgicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CentroCirurgicoService centroCirurgicoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testMarcarProcedimento() throws Exception {
        MarcarProcedimentoDTO request = new MarcarProcedimentoDTO();
        request.setCpfPaciente("123.456.789-00");
        request.setCodigoExame("1");
        request.setHorarioDesejado("01/01/2025 10:00");

        when(centroCirurgicoService.marcarProcedimento(any(MarcarProcedimentoDTO.class)))
            .thenReturn(ResponseDTO.builder()
                .mensagem("Procedimento marcado com sucesso")
                .codigo("P123")
                .build());

        mockMvc.perform(post("/api/procedimentos/marcar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
