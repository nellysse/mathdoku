package com.example.demo.model.dto;

public record CheckRequestDTO (
        String sessionId,
        String[][] userGrid // Обязательно String[][]
) {}