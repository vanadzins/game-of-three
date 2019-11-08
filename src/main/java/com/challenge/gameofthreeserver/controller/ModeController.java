package com.challenge.gameofthreeserver.controller;

import com.challenge.gameofthreeserver.controller.dto.ModeDto;
import com.challenge.gameofthreeserver.repository.PlayerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ModeController {

    private PlayerRepository playerRepository;

    @Autowired
    public ModeController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @PostMapping(
            path = "/mode",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public void setPlayerAutomatic(@RequestBody ModeDto dto) {
        log.info("Dto: " + dto);

        playerRepository.getPlayerWithSessionId(dto.getSessionId())
                .ifPresent(player -> player.setAutomatic(dto.isAutomatic()));
    }
}
