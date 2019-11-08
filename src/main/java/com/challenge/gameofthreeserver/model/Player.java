package com.challenge.gameofthreeserver.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Player {

    private String sessionId;
    private boolean automatic;
    private boolean available;

    public static Player newPlayer(String sessionId) {
        return new Player(
                sessionId,
                false,
                true
        );
    }
}
