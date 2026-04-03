package com.example.teste.tecnico.domain.model;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import com.example.teste.tecnico.exception.BusinessException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "cupom")
@SQLRestriction("status <> 'DELETED'")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String code;
    private String description;
    private double discountValue;
    private String expirationDate;
    private boolean published;
    private boolean redeemed;
    @Enumerated(EnumType.STRING)
    private Status status;

    private Cupom(String code, String description, double discountValue,
                  String expirationDate, boolean published, Status status) {
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.redeemed = false;
        this.status = Status.ACTIVE;
    }

    public static Cupom create(String code, String description,
                               double discountValue, String expirationDate,
                               boolean published) {
        validateExpirationDate(expirationDate);
        validateDiscountValue(discountValue);
        String sanitizedCode = sanitizeCode(code);
        return new Cupom(sanitizedCode, description, discountValue,
                expirationDate, published, Status.ACTIVE);
    }

    public void delete() {
        if (this.status == Status.DELETED) {
            throw new BusinessException("Coupon already deleted");
        }
        this.status = Status.DELETED;
    }

    private static void validateExpirationDate(String expirationDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate expiration = LocalDate.parse(expirationDate, formatter);
        if (!expiration.isAfter(LocalDate.now())) {
            throw new BusinessException("Expiration date must be in the future");
        }
    }

    private static void validateDiscountValue(double discountValue) {
        if (discountValue < 0.5) {
            throw new BusinessException("Discount value must be at least 0.5");
        }
    }

    private static String sanitizeCode(String code) {
        String sanitized = code.replaceAll("[^a-zA-Z0-9]", "");
        if (sanitized.isEmpty()) {
            throw new BusinessException("Code must contain at least one alphanumeric character");
        }
        return sanitized.substring(0, Math.min(6, sanitized.length()));
    }
}