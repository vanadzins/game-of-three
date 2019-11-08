package com.challenge.gameofthreeserver.controller.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ModeDto {

    String sessionId;
    boolean automatic;
}
