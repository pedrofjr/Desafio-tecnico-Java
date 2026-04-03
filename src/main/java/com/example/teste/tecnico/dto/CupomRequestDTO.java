package com.example.teste.tecnico.dto;

public record CupomRequestDTO(
    String code,
    String description,
    double discountValue,
    String expirationDate,
    boolean published
) {}