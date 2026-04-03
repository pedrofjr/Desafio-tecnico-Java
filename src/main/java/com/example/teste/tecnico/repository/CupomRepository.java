package com.example.teste.tecnico.repository;

import com.example.teste.tecnico.domain.model.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CupomRepository extends JpaRepository<Cupom, String>
{}