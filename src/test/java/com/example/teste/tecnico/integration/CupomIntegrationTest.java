package com.example.teste.tecnico.integration;

import com.example.teste.tecnico.domain.model.Cupom;
import com.example.teste.tecnico.repository.CupomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@DisplayName("Integracao -- CupomController (HTTP + H2)")
class CupomIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CupomRepository cupomRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    private static final LocalDate DATA_FUTURA = LocalDate.now().plusYears(2);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        cupomRepository.deleteAll();
    }

    @Test
    @DisplayName("[19] POST /cupons com dados validos deve retornar 201 e body correto")
    void postCoupon_dadosValidos_deveRetornar201EBodyCorreto() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "ABC123", "description", "Cupom de integracao",
                "discountValue", 0.8, "expirationDate", DATA_FUTURA.toString(), "published", false);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ABC123"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.published").value(false))
                .andExpect(jsonPath("$.redeemed").value(false));
    }

    @Test
    @DisplayName("[19b] POST /cupons com published=true deve retornar published=true")
    void postCoupon_publishedTrue_deveRetornarPublishedTrue() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "XYZ999", "description", "Publicado",
                "discountValue", 1.0, "expirationDate", DATA_FUTURA.toString(), "published", true);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.published").value(true));
    }

    @Test
    @DisplayName("[20] POST /cupons com expirationDate passada deve retornar 422")
    void postCoupon_dataPassada_deveRetornar422() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "ABC123", "description", "Expirado",
                "discountValue", 1.0, "expirationDate", "2020-01-01", "published", false);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("[20b] POST /cupons com expirationDate = hoje deve retornar 422")
    void postCoupon_dataHoje_deveRetornar422() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "ABC123", "description", "Expira hoje",
                "discountValue", 1.0, "expirationDate", LocalDate.now().toString(), "published", false);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is(422));
    }

    @Test
    @DisplayName("[21] POST /cupons com discountValue < 0.5 deve retornar 422")
    void postCoupon_descontoAbaixoDoMinimo_deveRetornar422() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "ABC123", "description", "Desconto invalido",
                "discountValue", 0.1, "expirationDate", DATA_FUTURA.toString(), "published", false);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is(422));
    }

    @Test
    @DisplayName("[21b] POST /cupons com discountValue = 0.5 deve retornar 201")
    void postCoupon_descontoMinimo_deveRetornar201() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "ABC123", "description", "Minimo",
                "discountValue", 0.5, "expirationDate", DATA_FUTURA.toString(), "published", false);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.discountValue").value(0.5));
    }

    @Test
    @DisplayName("[21c] POST /cupons com discountValue negativo deve retornar 400 (Bean Validation)")
    void postCoupon_descontoNegativo_deveRetornar400() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "ABC123", "description", "Negativo",
                "discountValue", -1.0, "expirationDate", DATA_FUTURA.toString(), "published", false);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.discountValue").exists());
    }

    @Test
    @DisplayName("[22] POST /cupons com code contendo especiais deve retornar code sanitizado")
    void postCoupon_codeComEspeciais_deveRetornarCodeSanitizado() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "AB-C1@23", "description", "Sanitizacao",
                "discountValue", 1.0, "expirationDate", DATA_FUTURA.toString(), "published", false);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ABC123"));
    }

    @Test
    @DisplayName("[23] DELETE /cupons/{id} para cupom existente deve retornar 204")
    void deleteCoupon_idValido_deveRetornar204() throws Exception {
        Cupom cupom = cupomRepository.save(Cupom.create("DEL001", "Para deletar", 1.0, DATA_FUTURA.toString(), false));
        mockMvc.perform(delete("/cupons/{id}", cupom.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("[24] DELETE /cupons/{id} apos soft-delete deve retornar 4xx")
    void deleteCoupon_cupomJaDeletado_deveRetornar4xx() throws Exception {
        Cupom cupom = cupomRepository.save(Cupom.create("DEL002", "Sera deletado", 1.0, DATA_FUTURA.toString(), false));
        cupom.delete();
        cupomRepository.save(cupom);
        // Mesma transacao: L1 cache retorna entidade com deleted=true → BusinessException (422)
        // Transacoes separadas: @SQLRestriction filtra o registro → NotFoundException (404)
        // Ambos os casos sao 4xx corretos.
        mockMvc.perform(delete("/cupons/{id}", cupom.getId()))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /cupons/{id} para cupom existente deve retornar 200")
    void getCouponById_idExistente_deveRetornar200() throws Exception {
        Cupom cupom = cupomRepository.save(Cupom.create("GET001", "Para buscar", 1.5, DATA_FUTURA.toString(), true));
        mockMvc.perform(get("/cupons/{id}", cupom.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET001"));
    }

    @Test
    @DisplayName("GET /cupons/{id} para id inexistente deve retornar 404")
    void getCouponById_idInexistente_deveRetornar404() throws Exception {
        mockMvc.perform(get("/cupons/{id}", "id-que-nao-existe"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /cupons com code em branco deve retornar 400 (Bean Validation)")
    void postCoupon_codeBranco_deveRetornar400() throws Exception {
        Map<String, Object> payload = Map.of(
                "code", "", "description", "Desc",
                "discountValue", 1.0, "expirationDate", DATA_FUTURA.toString(), "published", false);
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.code").exists());
    }

    @Test
    @DisplayName("POST /cupons sem body deve retornar 4xx")
    void postCoupon_semBody_deveRetornar4xx() throws Exception {
        mockMvc.perform(post("/cupons").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
