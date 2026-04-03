package com.example.teste.tecnico.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cupom")
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
    private Status status;
    private boolean deleted;

    public Cupom(String code, String description, double discountValue, String expirationDate, boolean published, Status status) {
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.published = published;
        this.redeemed = false;
        this.status = status;
        this.deleted = false;   
    }
}