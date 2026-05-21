package com.example.demo.model.dto;

public record GameResponseDTO(
        String sessionId,
        String difficulty,
        String[][] cells
) {}