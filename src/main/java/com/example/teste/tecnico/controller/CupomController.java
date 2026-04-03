package com.example.teste.tecnico.controller;

import com.example.teste.tecnico.dto.CupomRequestDTO;
import com.example.teste.tecnico.dto.CupomResponseDTO;
import com.example.teste.tecnico.service.CupomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coupon")
public class CupomController {
    private final CupomService cupomService;

    public CupomController(CupomService cupomService) {
        this.cupomService = cupomService;
    }

    @PostMapping
    public ResponseEntity<CupomResponseDTO> createCupom(@Valid @RequestBody CupomRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cupomService.createCupom(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CupomResponseDTO> getCupomById(@PathVariable String id) {
        return ResponseEntity.ok(cupomService.getCupomById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCupomById(@PathVariable String id) {
        cupomService.deleteCupomById(id);
        return ResponseEntity.noContent().build();
    }
}
