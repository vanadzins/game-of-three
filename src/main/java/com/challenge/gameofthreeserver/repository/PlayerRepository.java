package com.challenge.gameofthreeserver.repository;

import com.challenge.gameofthreeserver.model.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;

@Slf4j
@Component
public class PlayerRepository {

    private ArrayList<Player> players = new ArrayList<>();

    public void add(Player player) {
        log.info("Adding new Player: " + player);
        players.add(player);
    }

    public void removeWithSessionId(String sessionId) {
        players.removeIf(getPredicate(sessionId));
    }

    public Optional<Player> getPlayerWithSessionId(String sessionId) {
        return players.stream()
                .filter(getPredicate(sessionId))
                .findFirst();
    }

    public Optional<Player> getOpponentAndSetAvailableFalse(String sessionId) {
        return players.stream()
                .filter(not(getPredicate(sessionId)))
                .findFirst()
                .map(player -> {
                    player.setAvailable(false);
                    getPlayerWithSessionId(sessionId)
                            .ifPresent(player1 -> player1.setAvailable(false));

                    return player;
                });
    }

    private Predicate<Player> getPredicate(String sessionId) {
        return player -> player.getSessionId().equals(sessionId);
    }
}
