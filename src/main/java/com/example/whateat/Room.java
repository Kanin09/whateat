package com.example.whateat;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.security.SecureRandom;
import java.util.*;

@Document("room")
@Data
public class Room {

    @Id
    private String id;
    private String roomCode;

    @NotNull(message = "Owner user is required")
    private String ownerUser;


    @Min(value = 1, message = "Max users must be at least 1")
    private int maxUsers;

    @Min(value = 1, message = "Each member must be able to select at least 1 food type")
    private int maxFoodSelectionsPerMember;

    private List<String> members;
    private List<String> foodTypes;




    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private Map<String, Set<String>> memberFoodSelections = new HashMap<>();

    //memberFoodSelections ใช้ตัวนี้เป็น member < 0 , 1 <<<นับ >


    public static final List<String> DEFAULT_FOOD_TYPES = List.of(
            "thai", "japan", "cafe", "noodles", "bakery","water", "buffet"
    );



    public Room() {

        this.roomCode = generateRoomCode();
        this.members = new ArrayList<>();
        this.foodTypes = new ArrayList<>(DEFAULT_FOOD_TYPES);
        this.memberFoodSelections = new HashMap<>();
    }

    public boolean canJoin() {
        return members.size()+1 <= maxUsers;
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

    public Map<String, Set<String>> getMemberFoodSelections() {
        if (memberFoodSelections == null) {
            memberFoodSelections = new HashMap<>();
        }
        return memberFoodSelections;
    }

    public void setMemberFoodSelections(Map<String, Set<String>> memberFoodSelections) {
        this.memberFoodSelections = memberFoodSelections != null ? memberFoodSelections : new HashMap<>();
    }

    public void selectFood(String member, String foodType) {
        getMemberFoodSelections().putIfAbsent(member, new HashSet<>()); // ✅ ใช้ Getter ป้องกัน Null
        getMemberFoodSelections().get(member).add(foodType);
    }

    public boolean hasMemberSelectedFood(String member, String foodType) {
        return getMemberFoodSelections().containsKey(member) &&
                getMemberFoodSelections().get(member).contains(foodType);
    }

    public boolean canSelectMoreFood(String member) {
        return getMemberFoodSelections().getOrDefault(member, new HashSet<>()).size() < maxFoodSelectionsPerMember;
    }
}
