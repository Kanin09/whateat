package com.example.whateat;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.xml.stream.Location;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Document("room")
@Data
public class Room {

    @Id
    private String id;
    private String roomCode;
    private String ownerUser;
    private int countUsers;
    private int maxUsers;
    private List<String> members;
    private List<String> foodTypes;



    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;





    public Room() {

        this.roomCode = generateRoomCode();
        this.members = new ArrayList<>();
        this.countUsers = 0;
        this.foodTypes = new ArrayList<>();
    }

    public boolean canJoin() {
        return members.size()+1 < maxUsers;
    }

    public String generateRoomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }


}
