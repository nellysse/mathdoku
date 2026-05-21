package com.example.demo.model.dto;

public record CheckResponseDTO (
        boolean correct,
        String message,
        int[][] correctSolution
) {}