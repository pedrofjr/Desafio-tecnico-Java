package com.example.teste.tecnico.domain.model;

import com.example.teste.tecnico.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dominio -- Cupom")
class CupomTest {

    private static final String DATA_FUTURA = LocalDate.now().plusYears(2).toString();
    private static final String DATA_ONTEM = LocalDate.now().minusDays(1).toString();
    private static final String DATA_HOJE = LocalDate.now().toString();

    @Test
    @DisplayName("[1] create() com campos validos deve criar Cupom corretamente")
    void create_camposValidos_deveCriarCupomCorreto() {
        Cupom cupom = Cupom.create("ABC123", "Cupom de teste", 0.8, DATA_FUTURA, false);
        assertEquals("ABC123", cupom.getCode());
        assertEquals("Cupom de teste", cupom.getDescription());
        assertEquals(0.8, cupom.getDiscountValue());
        assertEquals(DATA_FUTURA, cupom.getExpirationDate());

        assertFalse(cupom.isPublished());
        assertFalse(cupom.isRedeemed());
        assertFalse(cupom.getStatus() == Status.DELETED);
        assertEquals(Status.ACTIVE, cupom.getStatus());
    }

    @Test
    @DisplayName("[2] code com especiais deve ser sanitizado")
    void create_codeComEspeciais_deveSerSanitizado() {
        Cupom cupom = Cupom.create("AB-C1@23", "desc", 1.0, DATA_FUTURA, false);
        assertEquals("ABC123", cupom.getCode());
    }

    @Test
    @DisplayName("[3] code sanitizado deve ter exatamente 6 caracteres")
    void create_codeSanitizado_deveTer6Caracteres() {
        Cupom cupom = Cupom.create("A!B@C#123", "desc", 1.0, DATA_FUTURA, false);
        assertEquals(6, cupom.getCode().length());
        assertTrue(cupom.getCode().matches("[a-zA-Z0-9]+"));
    }

    @Test
    @DisplayName("[4] discountValue = 0.5 deve ser aceito")
    void create_descontoMinimo_deveAceitar() {
        assertDoesNotThrow(() -> Cupom.create("ABC123", "desc", 0.5, DATA_FUTURA, false));
    }

    @Test
    @DisplayName("[5] discountValue < 0.5 deve lancar BusinessException")
    void create_descontoAbaixoDoMinimo_deveLancarBusinessException() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> Cupom.create("ABC123", "desc", 0.4, DATA_FUTURA, false));
        assertTrue(ex.getMessage().toLowerCase().contains("discount"));
    }

    @Test
    @DisplayName("[6] expirationDate no futuro deve ser aceita")
    void create_dataFutura_deveAceitar() {
        assertDoesNotThrow(() -> Cupom.create("ABC123", "desc", 1.0, DATA_FUTURA, false));
    }

    @Test
    @DisplayName("[7] expirationDate = hoje deve lancar BusinessException")
    void create_dataHoje_deveLancarBusinessException() {
        assertThrows(BusinessException.class,
                () -> Cupom.create("ABC123", "desc", 1.0, DATA_HOJE, false));
    }

    @Test
    @DisplayName("[8] expirationDate no passado deve lancar BusinessException")
    void create_dataPassada_deveLancarBusinessException() {
        assertThrows(BusinessException.class,
                () -> Cupom.create("ABC123", "desc", 1.0, DATA_ONTEM, false));
    }

    @Test
    @DisplayName("[9] create() com published=true deve setar published=true")
    void create_publishedTrue_deveSetarPublishedTrue() {
        assertTrue(Cupom.create("ABC123", "desc", 1.0, DATA_FUTURA, true).isPublished());
    }

    @Test
    @DisplayName("[10] create() com published=false deve ter published=false")
    void create_publishedFalse_deveSetarPublishedFalse() {
        assertFalse(Cupom.create("ABC123", "desc", 1.0, DATA_FUTURA, false).isPublished());
    }

    @Test
    @DisplayName("[11] delete() em cupom ativo deve setar status=DELETED")
    void delete_cupomAtivo_deveSetarDeletedTrue() {
        Cupom cupom = Cupom.create("ABC123", "desc", 1.0, DATA_FUTURA, false);
        assertFalse(cupom.getStatus() == Status.DELETED);
        cupom.delete();
        assertTrue(cupom.getStatus() == Status.DELETED);
    }

    @Test
    @DisplayName("[12] double-delete deve lancar BusinessException")
    void delete_cupomJaDeletado_deveLancarBusinessException() {
        Cupom cupom = Cupom.create("ABC123", "desc", 1.0, DATA_FUTURA, false);
        cupom.delete();
        BusinessException ex = assertThrows(BusinessException.class, cupom::delete);
        assertTrue(ex.getMessage().toLowerCase().contains("already deleted"));
    }
}
