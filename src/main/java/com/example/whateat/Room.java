package com.example.whateat;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;


@CrossOrigin(origins = "http://localhost:3000")
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
    private double latitude;
    private double longitude;
    private LocalDateTime randomizedAt; // เวลาที่สุ่มอาหาร
    private Map<String, Boolean> memberReadyStatus;
    private Map<String, LinkedList<String>> memberFoodSelections = new HashMap<>(); //memberFoodSelections ใช้ตัวนี้เป็น member < 0 , 1 <<<นับ >




    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;


    public static final List<String> DEFAULT_FOOD_TYPES = List.of(
            "ของหวาน", "อาหารตามสั่ง", "อาหารจานเดียว",
            "ก๋วยเตี๋ยว", "เครื่องดื่ม/น้ำผลไม้", "เบเกอรี/เค้ก",
            "ชาบู", "อาหารเกาหลี", "ปิ้งย่าง",
            "คาเฟ่", "บุฟเฟ่ต์"
    );



    public Room() {

        this.roomCode = generateRoomCode();
        this.members = new ArrayList<>();
        this.foodTypes = new ArrayList<>(DEFAULT_FOOD_TYPES);
        this.memberFoodSelections = new HashMap<>();
        this.memberReadyStatus = new HashMap<>();
    }


    // ✅ ใช้ตรวจสอบว่าห้องยังว่างให้คนเข้าร่วมหรือไม่
    public boolean canJoin() {
        return members.size()+1 <= maxUsers;
    }

    public boolean isAllMembersReady() {
        if (memberReadyStatus == null || memberReadyStatus.size() < members.size()) {
            return false;
        }
        for (String member : members) {
            if (!Boolean.TRUE.equals(memberReadyStatus.get(member))) {
                return false;
            }
        }
        return true;
    }


    // ✅ สร้างรหัสห้องสุ่ม
    public String generateRoomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }

    public Map<String, Boolean> getMemberReadyStatus() {
        if (memberReadyStatus == null) {
            memberReadyStatus = new HashMap<>();
        }
        return memberReadyStatus;
    }



    public Map<String, LinkedList<String>> getMemberFoodSelections() {
        if (memberFoodSelections == null) {
            memberFoodSelections = new HashMap<>();
        }
        return memberFoodSelections;
    }




}
