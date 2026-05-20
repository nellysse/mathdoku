package com.example.demo.model.entity;

import jakarta.persistence.*;
        import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
public class GameSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String difficulty;

    @Column(name = "solution_json", nullable = false, length = 1000)
    private String solutionJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    public GameSession() {}

    public GameSession(String difficulty, String solutionJson) {
        this.difficulty = difficulty;
        this.solutionJson = solutionJson;
    }

    public String getId() { return id; }
    public String getDifficulty() { return difficulty; }
    public String getSolutionJson() { return solutionJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setSolutionJson(String solutionJson) { this.solutionJson = solutionJson; }
}
