package com.example.teste.tecnico.service;

import com.example.teste.tecnico.domain.model.Cupom;
import com.example.teste.tecnico.dto.CupomRequestDTO;
import com.example.teste.tecnico.dto.CupomResponseDTO;
import com.example.teste.tecnico.exception.NotFoundException;
import com.example.teste.tecnico.repository.CupomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CupomService {

    private final CupomRepository cupomRepository;

    public CupomService(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    @Transactional
    public CupomResponseDTO createCupom(CupomRequestDTO dto) {
        Cupom cupom = Cupom.create(
                dto.code(),
                dto.description(),
                dto.discountValue(),
                dto.expirationDate(),
                dto.published()
        );
        cupomRepository.save(cupom);
        return toResponseDTO(cupom);
    }

    @Transactional(readOnly = true)
    public CupomResponseDTO getCupomById(String id) {
        Cupom cupom = cupomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found with id: " + id));
        return toResponseDTO(cupom);
    }

    @Transactional
    public void deleteCupomById(String id) {
        Cupom cupom = cupomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Coupon not found with id: " + id));
        cupom.delete();
        cupomRepository.save(cupom);
    }

    private CupomResponseDTO toResponseDTO(Cupom cupom) {
        return new CupomResponseDTO(
                cupom.getId(),
                cupom.getCode(),
                cupom.getDescription(),
                cupom.getDiscountValue(),
                cupom.getExpirationDate(),
                cupom.getStatus(),
                cupom.isPublished(),
                cupom.isRedeemed()
        );
    }
}