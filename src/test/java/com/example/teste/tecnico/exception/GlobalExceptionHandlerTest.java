package com.example.teste.tecnico.exception;

import com.example.teste.tecnico.controller.CupomController;
import com.example.teste.tecnico.dto.CupomRequestDTO;
import com.example.teste.tecnico.service.CupomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CupomController.class)
@DisplayName("Handler -- GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CupomService cupomService;

    private static final String BASE_URL = "/coupon";

    @Test
    @DisplayName("NotFoundException deve retornar 404 com campos status, error e message no body")
    void handleNotFoundException_deveRetornar404ComCamposCorretos() throws Exception {
        when(cupomService.getCupomById(anyString()))
                .thenThrow(new NotFoundException("Coupon not found with id: xyz"));

        mockMvc.perform(get(BASE_URL + "/{id}", "xyz"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Coupon not found with id: xyz"));
    }

    @Test
    @DisplayName("BusinessException deve retornar 422 com campos status, error e message no body")
    void handleBusinessException_deveRetornar422ComCamposCorretos() throws Exception {
        CupomRequestDTO request = new CupomRequestDTO("ABC123", "desc", 10.0, "2028-01-01", false);
        when(cupomService.createCupom(any(CupomRequestDTO.class)))
                .thenThrow(new BusinessException("Expiration date must be in the future"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Business Rule Violation"))
                .andExpect(jsonPath("$.message").value("Expiration date must be in the future"));
    }

    @Test
    @DisplayName("MethodArgumentNotValidException deve retornar 400 com fieldErrors no body")
    void handleValidationException_deveRetornar400ComFieldErrors() throws Exception {
        CupomRequestDTO requestInvalido = new CupomRequestDTO("", "", 10.0, "2028-01-01", false);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.code").value("code is required"))
                .andExpect(jsonPath("$.fieldErrors.description").value("description is required"));
    }

    @Test
    @DisplayName("HttpMessageNotReadableException deve retornar 400 com error correto no body")
    void handleNotReadableException_deveRetornar400ComError() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ malformed json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Malformed or missing request body"));
    }

    @Test
    @DisplayName("Exception genérica não tratada deve retornar 500 com campos corretos no body")
    void handleGenericException_deveRetornar500ComCamposCorretos() throws Exception {
        doThrow(new RuntimeException("Erro inesperado"))
                .when(cupomService).deleteCupomById(anyString());

        mockMvc.perform(delete(BASE_URL + "/{id}", "qualquer-id"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Erro inesperado"));
    }
}
