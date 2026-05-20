package com.example.demo.model.dto;

import java.util.List;

public record GameResponseDTO(
        String sessionId,
        String difficulty,
        String[][] cells
) {}