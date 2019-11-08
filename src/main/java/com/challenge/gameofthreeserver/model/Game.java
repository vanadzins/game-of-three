package com.challenge.gameofthreeserver.model;

import com.challenge.gameofthreeserver.util.Status;
import lombok.*;

import java.util.Set;
import java.util.function.Predicate;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Game {

    private int currentNumber;

    private Player nextMove;

    private Set<Player> players;

    private int status;

    public static Game newGame(int currentNumber, Player nextMove, Set<Player> players) {
        return new Game(
                currentNumber,
                nextMove,
                players,
                Status.NEW_GAME
        );
    }

    public boolean doesPlayerExists(String sessionId) {
        return players.stream().anyMatch(player -> player.getSessionId().equals(sessionId));
    }

    public Player getOpponent(String sessionId) {
        return getPlayerPredicate(player -> !player.getSessionId().equals(sessionId));
    }

    private Player getPlayerPredicate(Predicate<Player> predicate) {
        return players.stream()
                .filter(predicate)
                .findFirst()
                .orElse(null);
    }
}
