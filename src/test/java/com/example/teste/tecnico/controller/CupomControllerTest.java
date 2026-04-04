package com.example.teste.tecnico.controller;

import com.example.teste.tecnico.domain.model.Status;
import com.example.teste.tecnico.dto.CupomRequestDTO;
import com.example.teste.tecnico.dto.CupomResponseDTO;
import com.example.teste.tecnico.exception.BusinessException;
import com.example.teste.tecnico.exception.NotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CupomController.class)
@DisplayName("Controller -- CupomController")
class CupomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CupomService cupomService;

    private static final String BASE_URL = "/coupon";
    private static final String ID_VALIDO = "uuid-valido-123";

    private CupomResponseDTO buildResponseDTO() {
        return new CupomResponseDTO(
                ID_VALIDO,
                "ABC123",
                "Cupom de teste",
                10.0,
                "2028-01-01",
                Status.ACTIVE,
                false,
                false
        );
    }

    // ─────────────────────────────────────────────
    // POST /coupon
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("POST /coupon com dados válidos deve retornar 201 e o body correto")
    void createCupom_dadosValidos_deveRetornar201EBody() throws Exception {
        CupomRequestDTO request = new CupomRequestDTO("ABC123", "Cupom de teste", 10.0, "2028-01-01", false);
        CupomResponseDTO response = buildResponseDTO();
        when(cupomService.createCupom(any(CupomRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(ID_VALIDO))
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.discountValue").value(10.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.redeemed").value(false));
    }

    @Test
    @DisplayName("POST /coupon quando service lança BusinessException deve retornar 422")
    void createCupom_serviceLancaBusinessException_deveRetornar422() throws Exception {
        CupomRequestDTO request = new CupomRequestDTO("ABC123", "Cupom de teste", 10.0, "2020-01-01", false);
        when(cupomService.createCupom(any(CupomRequestDTO.class)))
                .thenThrow(new BusinessException("Expiration date must be in the future"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422));
    }

    @Test
    @DisplayName("POST /coupon com body inválido (Bean Validation) deve retornar 400")
    void createCupom_bodyInvalido_deveRetornar400() throws Exception {
        CupomRequestDTO request = new CupomRequestDTO("", "desc", 10.0, "2028-01-01", false);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────
    // GET /coupon/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("GET /coupon/{id} com id válido deve retornar 200 e o body correto")
    void getCupomById_idValido_deveRetornar200EBody() throws Exception {
        CupomResponseDTO response = buildResponseDTO();
        when(cupomService.getCupomById(ID_VALIDO)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/{id}", ID_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID_VALIDO))
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.discountValue").value(10.0))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /coupon/{id} com id inexistente deve retornar 404")
    void getCupomById_idInexistente_deveRetornar404() throws Exception {
        when(cupomService.getCupomById(eq("id-inexistente")))
                .thenThrow(new NotFoundException("Coupon not found with id: id-inexistente"));

        mockMvc.perform(get(BASE_URL + "/{id}", "id-inexistente"))
                .andExpect(status().isNotFound());
    }

    // ─────────────────────────────────────────────
    // DELETE /coupon/{id}
    // ─────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /coupon/{id} com id válido deve retornar 204 sem body")
    void deleteCupomById_idValido_deveRetornar204() throws Exception {
        doNothing().when(cupomService).deleteCupomById(ID_VALIDO);

        mockMvc.perform(delete(BASE_URL + "/{id}", ID_VALIDO))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /coupon/{id} com id inexistente deve retornar 404")
    void deleteCupomById_idInexistente_deveRetornar404() throws Exception {
        doThrow(new NotFoundException("Coupon not found with id: id-inexistente"))
                .when(cupomService).deleteCupomById("id-inexistente");

        mockMvc.perform(delete(BASE_URL + "/{id}", "id-inexistente"))
                .andExpect(status().isNotFound());
    }
}
