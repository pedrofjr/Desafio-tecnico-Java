package com.example.teste.tecnico.dto;

import com.example.teste.tecnico.domain.model.Status;

public record CupomResponseDTO(
    String id,
    String code,
    String description,
    double discountValue,
    String expirationDate,
    Status status,
    boolean published,
    boolean redeemed
) {}