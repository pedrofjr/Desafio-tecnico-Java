package com.example.teste.tecnico.service;

import com.example.teste.tecnico.domain.model.Cupom;
import com.example.teste.tecnico.domain.model.Status;
import com.example.teste.tecnico.dto.CupomRequestDTO;
import com.example.teste.tecnico.dto.CupomResponseDTO;
import com.example.teste.tecnico.exception.BusinessException;
import com.example.teste.tecnico.exception.NotFoundException;
import com.example.teste.tecnico.repository.CupomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Service -- CupomService")
class CupomServiceTest {

    @Mock
    private CupomRepository cupomRepository;

    @InjectMocks
    private CupomService cupomService;

    private static final LocalDate DATA_FUTURA = LocalDate.now().plusYears(2);
    private static final String ID_VALIDO = "uuid-valido-123";

    private Cupom cupomAtivo;
    private CupomRequestDTO dtoValido;

    @BeforeEach
    void setUp() {
        cupomAtivo = Cupom.create("ABC123", "Cupom de teste", 1.0, DATA_FUTURA.toString(), false);
        dtoValido = new CupomRequestDTO("ABC123", "Cupom de teste", 1.0, DATA_FUTURA.toString(), false);
    }

    @Test
    @DisplayName("[14] createCupom deve chamar repository.save() exatamente 1 vez")
    void createCupom_dadosValidos_deveChamarSaveUmaVez() {
        when(cupomRepository.save(any(Cupom.class))).thenReturn(cupomAtivo);
        CupomResponseDTO response = cupomService.createCupom(dtoValido);
        verify(cupomRepository, times(1)).save(any(Cupom.class));
        assertNotNull(response);
        assertEquals("ABC123", response.code());
        assertEquals(Status.ACTIVE, response.status());
    }

    @Test
    @DisplayName("[14b] createCupom com data futura deve retornar status ACTIVE")
    void createCupom_dataFutura_deveRetornarStatusActive() {
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(inv -> inv.getArgument(0));
        CupomResponseDTO response = cupomService.createCupom(dtoValido);
        assertEquals(Status.ACTIVE, response.status());
        verify(cupomRepository, atMostOnce()).save(any(Cupom.class));
    }

    @Test
    @DisplayName("[15] createCupom com data passada deve lancar BusinessException sem save()")
    void createCupom_dataExpirada_deveLancarBusinessExceptionSemSave() {
        CupomRequestDTO dto = new CupomRequestDTO("ABC123", "desc", 1.0, LocalDate.now().minusDays(1).toString(), false);
        assertThrows(BusinessException.class, () -> cupomService.createCupom(dto));
        verify(cupomRepository, never()).save(any(Cupom.class));
    }

    @Test
    @DisplayName("[15b] createCupom com data = hoje deve lancar BusinessException sem save()")
    void createCupom_dataHoje_deveLancarBusinessExceptionSemSave() {
        CupomRequestDTO dto = new CupomRequestDTO("ABC123", "desc", 1.0, LocalDate.now().toString(), false);
        assertThrows(BusinessException.class, () -> cupomService.createCupom(dto));
        verify(cupomRepository, never()).save(any(Cupom.class));
    }

    @Test
    @DisplayName("[15c] createCupom com discountValue < 0.5 deve lancar BusinessException sem save()")
    void createCupom_descontoAbaixoDoMinimo_deveLancarBusinessExceptionSemSave() {
        CupomRequestDTO dto = new CupomRequestDTO("ABC123", "desc", 0.4, DATA_FUTURA.toString(), false);
        assertThrows(BusinessException.class, () -> cupomService.createCupom(dto));
        verify(cupomRepository, never()).save(any(Cupom.class));
    }

    @Test
    @DisplayName("[16] deleteCupomById com id inexistente deve lancar NotFoundException")
    void deleteCupomById_idInexistente_deveLancarNotFoundException() {
        when(cupomRepository.findById("id-inexistente")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> cupomService.deleteCupomById("id-inexistente"));
        verify(cupomRepository, never()).save(any(Cupom.class));
    }

    @Test
    @DisplayName("[17] deleteCupomById para cupom ja deletado deve lancar BusinessException")
    void deleteCupomById_cupomJaDeletado_deveLancarBusinessException() {
        Cupom cupomDeletado = Cupom.create("ABC123", "desc", 1.0, DATA_FUTURA.toString(), false);
        cupomDeletado.delete();
        when(cupomRepository.findById(ID_VALIDO)).thenReturn(Optional.of(cupomDeletado));
        assertThrows(BusinessException.class, () -> cupomService.deleteCupomById(ID_VALIDO));
    }

    @Test
    @DisplayName("[18] deleteCupomById valido deve chamar save() com status=DELETED (soft-delete)")
    void deleteCupomById_cupomValido_deveChamarSaveComDeletedTrue() {
        when(cupomRepository.findById(ID_VALIDO)).thenReturn(Optional.of(cupomAtivo));
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(inv -> inv.getArgument(0));
        cupomService.deleteCupomById(ID_VALIDO);
        verify(cupomRepository, times(1)).save(argThat((Cupom c) -> c.getStatus() == Status.DELETED));
    }
}
