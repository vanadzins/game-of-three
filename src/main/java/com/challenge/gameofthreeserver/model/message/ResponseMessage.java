package com.challenge.gameofthreeserver.model.message;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ResponseMessage {

    private Integer number;
    private Integer status;
    private String message;
}
