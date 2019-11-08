package com.challenge.gameofthreeserver.service;

import com.challenge.gameofthreeserver.model.Game;
import com.challenge.gameofthreeserver.model.Player;
import com.challenge.gameofthreeserver.model.Response;
import com.challenge.gameofthreeserver.repository.GameRepository;
import com.challenge.gameofthreeserver.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class GameService {

    private GameRepository gameRepository;

    private PlayerRepository playerRepository;

    public GameService(
            GameRepository gameRepository,
            PlayerRepository playerRepository)
    {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
    }

    public Response startGame(final String sessionId, final int number) {
        if (number < 2) {
            return Response.errorResponse(sessionId, "Number is too small. Min = 2");
        }

        if (gameRepository.doesGameWithSessionIdExist(sessionId)) {
            log.info("Game with sessionId: " + sessionId + " already exists.");
            return Response.errorResponse(sessionId, "You are already playing a game!");
        }

        return playerRepository.getPlayerWithSessionId(sessionId)
                .map(currentPlayer ->
                        playerRepository.getOpponentAndSetAvailableFalse(sessionId)
                        .map(opponent -> {
                            gameRepository.add(Game.newGame(
                                    number,
                                    opponent,
                                    Stream.of(currentPlayer, opponent)
                                            .collect(Collectors.toSet())
                            ));

                            return Response.newGameResponse(sessionId, opponent, number);
                        })
                        .orElseGet(() -> {
                            String msg ="No opponent found";
                            log.info(msg);
                            return Response.errorResponse(sessionId, msg);
                        }))
                .orElseGet(() -> {
                    String msg = "Player not found";
                    log.info(msg);
                    return Response.errorResponse(sessionId, msg);
                });
    }

    public Response addition(int addition, String sessionId) {
        return gameRepository.getGameWithSessionId(sessionId).map(
                game -> {
                    if (!game.getNextMove().getSessionId().equals(sessionId)) {
                        return Response.errorResponse(sessionId, "It is not your move, wait for the opponent.");
                    }

                    int oldNumber = game.getCurrentNumber();
                    if (!hasNoRemainder(oldNumber, addition)) {
                        return Response.errorResponse(sessionId, (oldNumber + addition) + " does not divide by 3");
                    } else {
                        return processResponse(
                                oldNumber,
                                addition,
                                game,
                                sessionId
                        );
                    }
                })
                .orElse(Response.errorResponse(sessionId, "Game not found"));
    }

    private Response processResponse(int oldNumber, int addition, Game game, String sessionId) {
        int newNumber = calculateNumber(oldNumber, addition);
        game.setCurrentNumber(newNumber);
        Player opponent = game.getOpponent(sessionId);
        String calculation = createMessage(oldNumber, newNumber, addition);

        if (newNumber > 1) {
            game.setNextMove(opponent);
            return Response.successfulResponse(
                    sessionId,
                    opponent,
                    game.getCurrentNumber(),
                    calculation
            );
        } else {
            gameRepository.removeGame(game);
            game.getPlayers().forEach(player -> player.setAvailable(true));

            return Response.endGameResponse(
                    sessionId,
                    opponent,
                    newNumber,
                    calculation
            );
        }
    }

    private String createMessage(int oldNumber, int newNumber, int addition) {
        return "(" + oldNumber + (addition < 0 ? " - " : " + ") + Math.abs(addition) + ") / 3 = " + newNumber;
    }

    private int calculateNumber(int number, int addition) {
        return (number + addition) / 3;
    }

    private boolean hasNoRemainder(int number, int addition) {
        return (number + addition) % 3 == 0;
    }

    public Response automatic(Response response) {
        String sessionId = response.getNextPlayer().getSessionId();

        return gameRepository.getGameWithSessionId(sessionId).map(
                game -> {
                    int number = game.getCurrentNumber();
                    int addition = hasNoRemainder(number, 1) ? 1 :
                            hasNoRemainder(number, 0) ? 0 : -1;

                    return processResponse(
                            number,
                            addition,
                            game,
                            sessionId
                    );
                })
                .orElse(Response.errorResponse(sessionId, "Game not found"));
    }
}
