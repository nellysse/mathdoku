package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.model.dto.CheckResponseDTO;
import com.example.demo.model.dto.GameResponseDTO;
import com.example.demo.model.entity.GameSession;
import com.example.demo.repository.GameSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;

@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final GameSessionRepository sessionRepository;
    private final LatexGeneratorService latexGenerator;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    private static final int[][] FALLBACK_SOLUTION = {
            {5, 3, 4, 6, 7, 8, 9, 1, 2},
            {6, 7, 2, 1, 9, 5, 3, 4, 8},
            {1, 9, 8, 3, 4, 2, 5, 6, 7},
            {8, 5, 9, 7, 6, 1, 4, 2, 3},
            {4, 2, 6, 8, 5, 3, 7, 9, 1},
            {7, 1, 3, 9, 2, 4, 8, 5, 6},
            {9, 6, 1, 5, 3, 7, 2, 8, 4},
            {2, 8, 7, 4, 1, 9, 6, 3, 5},
            {3, 4, 5, 2, 8, 6, 1, 7, 9}
    };

    public GameService(GameSessionRepository sessionRepository,
                       LatexGeneratorService latexGenerator,
                       ObjectMapper objectMapper) {
        this.sessionRepository = sessionRepository;
        this.latexGenerator = latexGenerator;
        this.objectMapper = objectMapper;
    }

    public GameResponseDTO createNewGame(String difficulty) {
        int[][] solution = fetchSolutionFromApi();
        String[][] mask = generateMask(solution, difficulty);

        String solutionJson = serializeSolution(solution);
        GameSession session = new GameSession(difficulty, solutionJson);
        GameSession saved = sessionRepository.save(session);

        log.info("New game created: sessionId={}, difficulty={}", saved.getId(), difficulty);

        // ВОТ ЗДЕСЬ нужно добавить null в качестве четвертого аргумента:
        return new GameResponseDTO(saved.getId(), difficulty, mask, null);
    }
    public CheckResponseDTO checkAnswer(String sessionId, String[][] userGrid) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        int[][] solution = deserializeSolution(session.getSolutionJson());

        boolean correct = true;
        outer:
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                String cell = userGrid[row][col];

                if (cell == null || cell.isBlank() || cell.equals("0")) {
                    correct = false;
                    break outer;
                }

                int val;
                try {
                    val = Integer.parseInt(cell.trim());
                } catch (NumberFormatException e) {
                    log.warn("Non-numeric value at [{},{}]: '{}'", row, col, cell);
                    correct = false;
                    break outer;
                }

                if (val != solution[row][col]) {
                    correct = false;
                    break outer;
                }
            }
        }

        if (correct) {
            session.setCompleted(true);
            sessionRepository.save(session);
            log.info("Session {} completed successfully.", sessionId);
            return new CheckResponseDTO(true, "Correct! You did it.", null);
        } else {
            log.info("Session {} — wrong answer submitted.", sessionId);
            return new CheckResponseDTO(false, "Errors. Try again.", solution);
        }
    }

    public int[][] getSolution(String sessionId) {
        GameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));
        return deserializeSolution(session.getSolutionJson());
    }

    public Integer getHint(String sessionId, int row, int col) {
        int[][] solution = getSolution(sessionId);
        if (row < 0 || row >= 9 || col < 0 || col >= 9) return null;
        return solution[row][col];
    }

    private int[][] fetchSolutionFromApi() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://sudoku-api.vercel.app/api/dosuku"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Dosuku API response status: {}", response.statusCode());

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode solutionNode = root.path("newboard").path("grids").get(0).path("solution");

            int[][] solution = new int[9][9];
            for (int row = 0; row < 9; row++) {
                for (int col = 0; col < 9; col++) {
                    solution[row][col] = solutionNode.get(row).get(col).asInt();
                }
            }

            log.info("Solution fetched from Dosuku API successfully.");
            return solution;

        } catch (Exception e) {
            log.warn("Dosuku API unavailable ({}). Using fallback solution.", e.getMessage());
            int[][] copy = new int[9][9];
            for (int i = 0; i < 9; i++) {
                copy[i] = FALLBACK_SOLUTION[i].clone();
            }
            return copy;
        }
    }

    private String[][] generateMask(int[][] solution, String difficulty) {
        double emptyThreshold = getEmptyThreshold(difficulty);
        double digitThreshold = emptyThreshold + getDigitThreshold(difficulty);

        String[][] mask = new String[9][9];

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                double roll = random.nextDouble();

                if (roll < emptyThreshold) {
                    mask[row][col] = "0";
                } else if (roll < digitThreshold) {
                    mask[row][col] = String.valueOf(solution[row][col]);
                } else {
                    mask[row][col] = latexGenerator.toLatex(solution[row][col], difficulty);
                }
            }
        }

        return mask;
    }

    private double getEmptyThreshold(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy"   -> 0.40;
            case "medium" -> 0.55;
            case "hard"   -> 0.65;
            default       -> 0.40;
        };
    }

    private double getDigitThreshold(String difficulty) {
        return switch (difficulty.toLowerCase()) {
            case "easy"   -> 0.45;
            case "medium" -> 0.20;
            case "hard"   -> 0.10;
            default       -> 0.45;
        };
    }

    private String serializeSolution(int[][] solution) {
        try {
            return objectMapper.writeValueAsString(solution);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize solution", e);
        }
    }

    private int[][] deserializeSolution(String json) {
        try {
            return objectMapper.readValue(json, int[][].class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize solution", e);
        }
    }
}