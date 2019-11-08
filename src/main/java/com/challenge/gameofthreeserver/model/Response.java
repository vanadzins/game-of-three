package com.challenge.gameofthreeserver.model;

import com.challenge.gameofthreeserver.util.Status;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Response {

    private Integer number;
    private Integer status;
    private String currentPlayerMessage;
    private String nextPlayerMessage;
    private String currentPlayer;
    private Player nextPlayer;

    public static Response errorResponse(String currentPlayer, String currentPlayerMessage) {
        return new Response(
                0,
                Status.ERROR,
                currentPlayerMessage,
                null,
                currentPlayer,
                null
        );
    }

    public static Response newGameResponse(String currentPlayer, Player nextPlayer, int number) {
        String msg = "The number is: " + number;

        return new Response(
                number,
                Status.NEW_GAME,
                msg + ". Wait until opponent finishes his turn.",
                msg,
                currentPlayer,
                nextPlayer
        );
    }

    public static Response successfulResponse(String currentPlayer, Player nextPlayer, int number, String message) {
        return new Response(
                number,
                Status.SUCCESSFUL,
                "You: " + message,
                "Opponent: " + message,
                currentPlayer,
                nextPlayer
        );
    }

    public static Response endGameResponse(String currentPlayer, Player nextPlayer, int number, String message) {
        return new Response(
                number,
                Status.END_GAME,
                message +" You won the game!",
                message + " You lost the game.",
                currentPlayer,
                nextPlayer
        );
    }
}
