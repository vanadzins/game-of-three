package com.challenge.gameofthreeserver.repository;

import com.challenge.gameofthreeserver.model.Game;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Predicate;

@Component
public class GameRepository {

    private ArrayList<Game> games = new ArrayList<>();

    public void add(Game game) {
        games.add(game);
    }

    public void removeGame(Game game) {
        games.remove(game);
    }

    public void removeGameWithSessionId(String sessionId) {
        games.removeIf(getGameExistPredicate(sessionId));
    }

    public Optional<Game> getGameWithSessionId(String sessionId) {
        return games.stream()
                .filter(getGameExistPredicate(sessionId))
                .findFirst();
    }

    public boolean doesGameWithSessionIdExist(String sessionId) {
        return games.stream().anyMatch(getGameExistPredicate(sessionId));
    }

    private Predicate<Game> getGameExistPredicate(String sessionId) {
        return game -> game.doesPlayerExists(sessionId);
    }
}
