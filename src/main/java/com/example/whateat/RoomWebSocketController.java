package com.example.whateat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class RoomWebSocketController {


    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastRoomUpdate(String roomCode, Object payload) {
        messagingTemplate.convertAndSend("/topic/room/" + roomCode, payload);
    }
}
