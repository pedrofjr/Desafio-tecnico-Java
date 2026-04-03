package com.example.teste.tecnico.service;

import com.example.teste.tecnico.domain.model.Cupom;
import com.example.teste.tecnico.domain.model.Status;
import com.example.teste.tecnico.dto.CupomRequestDTO;
import com.example.teste.tecnico.dto.CupomResponseDTO;
import com.example.teste.tecnico.exception.NotFoundException;
import com.example.teste.tecnico.repository.CupomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class CupomService {
    private final CupomRepository cupomRepository;

    public CupomService(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    private boolean isExpired(String expirationDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate expiration = LocalDate.parse(expirationDate, formatter);
        LocalDate today = LocalDate.now();
        return expiration.isBefore(today);
    }

    private Status statusFromExpirationDate(String expirationDate) {
        return isExpired(expirationDate) ? Status.EXPIRED : Status.ACTIVE;
    }

    public CupomResponseDTO createCupom(CupomRequestDTO cupomRequestDTO) {
        Cupom cupom = new Cupom(
            cupomRequestDTO.code(),
            cupomRequestDTO.description(),
            cupomRequestDTO.discountValue(),
            cupomRequestDTO.expirationDate(),
            cupomRequestDTO.published(),
            statusFromExpirationDate(cupomRequestDTO.expirationDate())
        );

        cupomRepository.save(cupom);

        CupomResponseDTO cupomResponseDTO = new CupomResponseDTO(
            cupom.getId(),
            cupom.getCode(),
            cupom.getDescription(),
            cupom.getDiscountValue(),
            cupom.getExpirationDate(),
            cupom.getStatus(),
            cupom.isPublished(),
            cupom.isRedeemed()
        );
        return cupomResponseDTO;
    }

    @Transactional
    public void deleteCupomById(String id) {
        Cupom cupom = cupomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found with id: " + id));
        cupom.delete();
        cupomRepository.save(cupom);
    }

    public Cupom getCupomByCode(String code) {
        return cupomRepository.findByCode(code);
    }

    public void deleteCupomByCode(String code) {
        cupomRepository.deleteByCode(code);
    }
}