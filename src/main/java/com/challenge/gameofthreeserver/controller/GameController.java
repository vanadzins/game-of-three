package com.challenge.gameofthreeserver.controller;

import com.challenge.gameofthreeserver.model.Response;
import com.challenge.gameofthreeserver.model.message.GameMessage;
import com.challenge.gameofthreeserver.model.message.ResponseMessage;
import com.challenge.gameofthreeserver.service.GameService;
import com.challenge.gameofthreeserver.util.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class GameController {

    private GameService service;

    private SimpMessagingTemplate template;

    private static final String DESTINATION = "/queue/result";

    @Autowired
    public GameController(GameService service, SimpMessagingTemplate template) {
        this.service = service;
        this.template = template;
    }

    @MessageMapping("/start")
    public void start(@Payload final int number, @Header("simpSessionId") String sessionId) {
        log.info("Message 'New Game' received with number: " + number);

        processResponse(service.startGame(sessionId, number));
    }

    @MessageMapping("/addition")
    public void add(@Payload final GameMessage message, @Header("simpSessionId") String sessionId) {
        log.info("Message 'Addition' received: with content: " + message);

        processResponse(service.addition(
                message.getAddition(),
                sessionId
        ));
    }

    private void processResponse(Response response) {
        sendToUser(
                new ResponseMessage(
                        response.getNumber(),
                        response.getStatus(),
                        response.getCurrentPlayerMessage()
                ),
                response.getCurrentPlayer()
        );

        if (response.getNextPlayer() != null) {
            sendToUser(
                    new ResponseMessage(
                            response.getNumber(),
                            response.getStatus(),
                            response.getNextPlayerMessage()
                    ),
                    response.getNextPlayer().getSessionId()
            );

            if (response.getNextPlayer().isAutomatic()
                    && (response.getStatus().equals(Status.SUCCESSFUL)
                    || response.getStatus().equals(Status.NEW_GAME))) {
                processResponse(service.automatic(response));
            }
        }
    }

    private void sendToUser(ResponseMessage message, String receiver) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setLeaveMutable(true);
        headerAccessor.setSessionId(receiver);

        template.convertAndSendToUser(
                receiver,
                DESTINATION,
                message,
                headerAccessor.getMessageHeaders()
        );

        log.info("Message (status: " + message.getStatus() + ") sent to: " + receiver);
    }

    @MessageExceptionHandler
    public void handleException(Throwable e) {
        e.printStackTrace();
    }
}
