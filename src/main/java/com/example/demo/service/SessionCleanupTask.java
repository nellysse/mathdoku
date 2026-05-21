package com.example.demo.service;

import com.example.demo.repository.GameSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class SessionCleanupTask {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupTask.class);
    private final GameSessionRepository sessionRepository;

    public SessionCleanupTask(GameSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupOldSessions() {
        LocalDateTime yesterday = LocalDateTime.now().minusHours(24);
        var oldSessions = sessionRepository.findAll().stream()
                .filter(session -> session.getCreatedAt() != null && session.getCreatedAt().isBefore(yesterday))
                .toList();

        if (!oldSessions.isEmpty()) {
            sessionRepository.deleteAll(oldSessions);
            log.info("Successfully deleted {} old game sessions.", oldSessions.size());
        }
    }
}
