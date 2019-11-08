package com.challenge.gameofthreeserver.service;

import com.challenge.gameofthreeserver.model.Game;
import com.challenge.gameofthreeserver.model.Player;
import com.challenge.gameofthreeserver.model.Response;
import com.challenge.gameofthreeserver.repository.GameRepository;
import com.challenge.gameofthreeserver.repository.PlayerRepository;
import com.challenge.gameofthreeserver.util.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @InjectMocks
    private GameService service;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private PlayerRepository playerRepository;

    private static String SESSION_ID = "random";

    @Test
    void startGame_with_number_smaller_than_2_should_return_error_response() {
        Response response = service.startGame(SESSION_ID, 1);

        assertThat(response.getStatus(), is(Status.ERROR));
    }

    @Test
    void startGame_should_return_error_response_when_game_exists() {
        when(gameRepository.doesGameWithSessionIdExist(SESSION_ID))
                .thenReturn(true);

        Response response = service.startGame(SESSION_ID, 3);

        assertThat(response.getStatus(), is(Status.ERROR));
    }

    @Test
    void startGame_should_return_error_response_when_player_does_not_exist() {
        when(gameRepository.doesGameWithSessionIdExist(anyString()))
                .thenReturn(false);

        when(playerRepository.getPlayerWithSessionId(anyString()))
                .thenReturn(Optional.empty());

        Response response = service.startGame(SESSION_ID, 3);

        assertThat(response.getStatus(), is(Status.ERROR));
    }

    @Test
    void startGame_should_return_error_response_when_no_opponent_player_exist() {
        when(gameRepository.doesGameWithSessionIdExist(anyString()))
                .thenReturn(false);

        when(playerRepository.getPlayerWithSessionId(anyString()))
                .thenReturn(Optional.of(Player.newPlayer(SESSION_ID)));

        when(playerRepository.getOpponentAndSetAvailableFalse(anyString()))
                .thenReturn(Optional.empty());

        Response response = service.startGame(SESSION_ID, 3);

        assertThat(response.getStatus(), is(Status.ERROR));
    }

    @Test
    void startGame_should_return_newGame_response_and_create_new_game() {
        when(gameRepository.doesGameWithSessionIdExist(anyString()))
                .thenReturn(false);

        when(playerRepository.getPlayerWithSessionId(anyString()))
                .thenReturn(Optional.of(Player.newPlayer(SESSION_ID)));

        when(playerRepository.getOpponentAndSetAvailableFalse(anyString()))
                .thenReturn(Optional.of(Player.newPlayer("session")));

        Response response = service.startGame(SESSION_ID, 3);

        assertThat(response.getStatus(), is(Status.NEW_GAME));
        verify(gameRepository, times(1)).add(any());
    }

    @Test
    void addition_should_return_error_response_when_game_does_not_exist() {
        when(gameRepository.getGameWithSessionId(anyString()))
                .thenReturn(Optional.empty());

        Response response = service.addition(0, SESSION_ID);

        assertThat(response.getStatus(), is(Status.ERROR));
    }

    @Test
    void addition_should_return_error_response_when_next_move_not_equals_sessionId() {
        when(gameRepository.getGameWithSessionId(anyString()))
                .thenReturn(Optional.of(Game.newGame(
                        3,
                        Player.newPlayer("different"),
                        null
                )));

        Response response = service.addition(0, SESSION_ID);

        assertThat(response.getStatus(), is(Status.ERROR));
    }

    @Test
    void addition_should_return_error_response_when_numbers_do_not_divide_by_3() {
        int currentNumber = 3;
        int addition = 1;
        Player currentPlayer = Player.newPlayer(SESSION_ID);

        when(gameRepository.getGameWithSessionId(anyString()))
                .thenReturn(Optional.of(Game.newGame(
                        currentNumber,
                        Player.newPlayer(SESSION_ID),
                        new HashSet<>() {{
                            add(currentPlayer);
                            add(Player.newPlayer("opponent"));
                        }}
                )));

        Response response = service.addition(addition, SESSION_ID);

        assertThat(response.getStatus(), is(Status.ERROR));
    }

    @Test
    void addition_should_return_successful_response_when_remaining_number_bigger_than_1() {
        int currentNumber = 5;
        int addition = 1;
        Player currentPlayer = Player.newPlayer(SESSION_ID);

        when(gameRepository.getGameWithSessionId(anyString()))
                .thenReturn(Optional.of(Game.newGame(
                        currentNumber,
                        Player.newPlayer(SESSION_ID),
                        new HashSet<>() {{
                            add(currentPlayer);
                            add(Player.newPlayer("opponent"));
                        }}
                )));

        Response response = service.addition(addition, SESSION_ID);

        assertThat(response.getStatus(), is(Status.SUCCESSFUL));
    }

    @Test
    void addition_should_return_endGame_response_when_remaining_number_is_1() {
        int currentNumber = 3;
        int addition = 0;
        Player currentPlayer = Player.newPlayer(SESSION_ID);

        when(gameRepository.getGameWithSessionId(anyString()))
                .thenReturn(Optional.of(Game.newGame(
                        currentNumber,
                        Player.newPlayer(SESSION_ID),
                        new HashSet<>() {{
                            add(currentPlayer);
                            add(Player.newPlayer("opponent"));
                        }}
                )));

        Response response = service.addition(addition, SESSION_ID);

        assertThat(response.getStatus(), is(Status.END_GAME));
    }

    @Test
    void automatic_should_return_error_response_when_game_does_not_exist() {
        Player nextPlayer = Player.newPlayer(SESSION_ID);

        when(gameRepository.getGameWithSessionId(anyString()))
                .thenReturn(Optional.empty());

        Response response = service.automatic(
                Response.successfulResponse(
                        null,
                        nextPlayer,
                        0,
                        ""
                )
        );

        assertThat(response.getStatus(), is(Status.ERROR));
    }

    @Test
    void automatic_should_return_successful_response_when_remaining_number_bigger_than_1() {
        int currentNumber = 5;
        Player currentPlayer = Player.newPlayer(SESSION_ID);
        Player nextPlayer = Player.newPlayer("opponent");

        when(gameRepository.getGameWithSessionId(anyString()))
                .thenReturn(Optional.of(Game.newGame(
                        currentNumber,
                        currentPlayer,
                        new HashSet<>() {{
                            add(currentPlayer);
                            add(Player.newPlayer("opponent"));
                        }}
                )));

        Response response = service.automatic(
                Response.successfulResponse(
                        SESSION_ID,
                        nextPlayer,
                        5,
                        ""
                )
        );

        assertThat(response.getStatus(), is(Status.SUCCESSFUL));
    }

    @Test
    void automatic_should_return_endGame_response_when_remaining_number_is_1() {
        int currentNumber = 3;
        Player currentPlayer = Player.newPlayer(SESSION_ID);
        Player nextPlayer = Player.newPlayer("opponent");

        when(gameRepository.getGameWithSessionId(anyString()))
                .thenReturn(Optional.of(Game.newGame(
                        currentNumber,
                        currentPlayer,
                        new HashSet<>() {{
                            add(currentPlayer);
                            add(Player.newPlayer("opponent"));
                        }}
                )));

        Response response = service.automatic(
                Response.successfulResponse(
                        SESSION_ID,
                        nextPlayer,
                        3,
                        ""
                )
        );

        assertThat(response.getStatus(), is(Status.END_GAME));
    }
}