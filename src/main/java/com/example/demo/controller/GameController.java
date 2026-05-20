package com.example.demo.controller;

import com.example.demo.model.dto.CheckRequestDTO;
import com.example.demo.model.dto.CheckResponseDTO;
import com.example.demo.model.dto.GameResponseDTO;
import com.example.demo.service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/new")
    public ResponseEntity<GameResponseDTO> newGame(
            @RequestParam(defaultValue = "easy") String difficulty) {

        log.info("Request: GET /api/game/new?difficulty={}", difficulty);

        if (!difficulty.matches("easy|medium|hard")) {
            return ResponseEntity.badRequest().build();
        }

        GameResponseDTO response = gameService.createNewGame(difficulty);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check")
    public ResponseEntity<CheckResponseDTO> checkAnswer(@RequestBody CheckRequestDTO request) {
        log.info("Request: POST /api/game/check, sessionId={}", request.sessionId());

        if (request.sessionId() == null || request.userGrid() == null
                || request.userGrid().length != 9) {
            return ResponseEntity.badRequest().build();
        }

        for (String[] row : request.userGrid()) {
            if (row == null || row.length != 9) {
                return ResponseEntity.badRequest().build();
            }
        }

        try {
            CheckResponseDTO response = gameService.checkAnswer(request.sessionId(), request.userGrid());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Session not found: {}", request.sessionId());
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/{sessionId}/hint")
    public ResponseEntity<Integer> getHint(
            @PathVariable String sessionId,
            @RequestParam int row,
            @RequestParam int col) {
        try {
            Integer hint = gameService.getHint(sessionId, row, col);
            if (hint == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(hint);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{sessionId}/validate")
    public ResponseEntity<Boolean> validateCell(
            @PathVariable String sessionId,
            @RequestParam int row,
            @RequestParam int col,
            @RequestParam int value) {
        try {
            Integer correctValue = gameService.getHint(sessionId, row, col);
            if (correctValue == null) return ResponseEntity.badRequest().build();
            return ResponseEntity.ok(correctValue == value);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}