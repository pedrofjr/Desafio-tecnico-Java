package com.example.teste.tecnico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record CupomRequestDTO(
    @NotBlank(message = "code is required")
    String code,

    @NotBlank(message = "description is required")
    String description,

    @Positive(message = "discountValue must be greater than zero")
    double discountValue,

    @NotBlank(message = "expirationDate is required")
    String expirationDate,

    boolean published
) {}