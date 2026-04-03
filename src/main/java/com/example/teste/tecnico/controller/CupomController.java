package com.example.teste.tecnico.controller;

import com.example.teste.tecnico.domain.model.Cupom;
import com.example.teste.tecnico.dto.CupomRequestDTO;
import com.example.teste.tecnico.dto.CupomResponseDTO;
import com.example.teste.tecnico.service.CupomService;
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
@RequestMapping("/cupons")
public class CupomController {
    private final CupomService cupomService;

    public CupomController(CupomService cupomService) {
        this.cupomService = cupomService;
    }

    @PostMapping
    public ResponseEntity<CupomResponseDTO> createCupom(@RequestBody CupomRequestDTO cupomRequestDTO) {
        CupomResponseDTO cupomResponseDTO = cupomService.createCupom(cupomRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(cupomResponseDTO);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Cupom> getCupomByCode(@PathVariable String code) {
        Cupom cupom = cupomService.getCupomByCode(code);
        if (cupom != null) {
            return ResponseEntity.ok(cupom);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteCupomByCode(@PathVariable String code) {
        cupomService.deleteCupomByCode(code);
        return ResponseEntity.noContent().build();
    }
}