package com.challenge.gameofthreeserver.event;

import com.challenge.gameofthreeserver.model.Player;
import com.challenge.gameofthreeserver.repository.GameRepository;
import com.challenge.gameofthreeserver.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
public class GameEventListener {

    private GameRepository gameRepository;

    private PlayerRepository playerRepository;

    @Autowired
    public GameEventListener(GameRepository gameRepository, PlayerRepository playerRepository) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    @EventListener
    private void handleSessionConnected(SessionConnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        log.info("New connection: " + sessionId);

        playerRepository.add(Player.newPlayer(sessionId));
    }

    @EventListener
    private void handleSessionDisconnected(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        log.info("Player disconnected: " + sessionId);

        gameRepository.removeGameWithSessionId(sessionId);
        playerRepository.removeWithSessionId(sessionId);
    }
}
