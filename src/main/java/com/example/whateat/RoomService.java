package com.example.whateat;


import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GoogleMapsService googleMapsService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Room createRoom(Room room) {
        if (room.getMaxUsers() <= 0) {
            throw new IllegalArgumentException("Max users must be greater than 0.");
        }

        room.setRoomCode(room.getRoomCode() != null ? room.getRoomCode() : room.generateRoomCode());

        if (room.getMembers() == null) room.setMembers(new ArrayList<>());
        if (room.getOwnerUser() != null && !room.getMembers().contains(room.getOwnerUser())) {
            room.getMembers().add(room.getOwnerUser());
        }

        if (room.getMaxFoodSelectionsPerMember() <= 0) {
            throw new ValidationException("Max food selections per member must be at least 1.");
        }

        room.setFoodTypes(new ArrayList<>(Room.DEFAULT_FOOD_TYPES));
        room.setLatitude(13.779322);
        room.setLongitude(100.560633);

        return roomRepository.save(room);
    }

    public Optional<Room> getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode);
    }

    @Transactional
    public ResponseEntity<String> deleteRoom(String roomCode, String ownerUser) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwnerUser().equals(ownerUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner can delete this room.");
        }

        roomRepository.delete(room);
        messagingTemplate.convertAndSend("/topic/room-deleted/" + roomCode, "Room has been deleted.");
        return ResponseEntity.ok("Room deleted successfully.");
    }

    @Transactional
    public ResponseEntity<String> leaveRoom(String roomCode, String memberName) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getMembers().contains(memberName)) {
            return ResponseEntity.badRequest().body("Member not found in the room!");
        }

        room.getMembers().remove(memberName);
        if (room.getMemberFoodSelections() != null) {
            room.getMemberFoodSelections().remove(memberName);
        }
        if (room.getMemberReadyStatus() != null) {
            room.getMemberReadyStatus().remove(memberName);
        }

        boolean wasOwner = room.getOwnerUser().equals(memberName);

        if (wasOwner) {
            if (!room.getMembers().isEmpty()) {
                room.setOwnerUser(room.getMembers().get(0));
            } else {
                roomRepository.delete(room);
                messagingTemplate.convertAndSend("/topic/room-deleted/" + roomCode, "Room deleted because no members left.");
                return ResponseEntity.ok("Room deleted because no members left.");
            }
        }

        roomRepository.save(room);

        // ✅ broadcast updated members
        messagingTemplate.convertAndSend("/topic/members/" + roomCode, room.getMembers());

        return ResponseEntity.ok("Member " + memberName + " has left the room.");
    }

    @Transactional
    public ResponseEntity<String> kickMember(String roomCode, String ownerUser, String memberName) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwnerUser().equals(ownerUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner can kick members.");
        }

        if (memberName == null || memberName.isEmpty()) {
            return ResponseEntity.badRequest().body("MemberName is required!");
        }

        if (!room.getMembers().contains(memberName)) {
            return ResponseEntity.badRequest().body("Member not found in the room!");
        }

        if (ownerUser.equals(memberName)) {
            return ResponseEntity.badRequest().body("Owner cannot kick themselves!");
        }

        room.getMembers().remove(memberName);
        if (room.getMemberFoodSelections() != null) {
            room.getMemberFoodSelections().remove(memberName);
        }
        if (room.getMemberReadyStatus() != null) {
            room.getMemberReadyStatus().remove(memberName);
        }

        roomRepository.save(room);
        messagingTemplate.convertAndSend("/topic/members/" + roomCode, room.getMembers());

        return ResponseEntity.ok("Member " + memberName + " has been kicked from the room.");
    }

    @Transactional
    public Room joinRoom(String roomCode, String memberName) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.canJoin()) {
            throw new RuntimeException("Room is full!");
        }

        if (room.getMembers().contains(memberName)) {
            throw new RuntimeException("Member already in the room!");
        }

        room.getMembers().add(memberName);
        Room saved = roomRepository.save(room);

        messagingTemplate.convertAndSend("/topic/members/" + roomCode, saved.getMembers());

        return saved;
    }

    @Transactional
    public ResponseEntity<?> selectFood(String roomCode, String member, String foodType) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getMembers().contains(member)) {
            return ResponseEntity.badRequest().body("Member not found in the room!");
        }

        if (!room.getFoodTypes().contains(foodType)) {
            return ResponseEntity.badRequest().body("Invalid food type!");
        }

        if (room.getMemberReadyStatus().getOrDefault(member, false)) {
            return ResponseEntity.badRequest().body(Map.of("error", "คุณกดพร้อมแล้ว ไม่สามารถเลือกอาหารเพิ่มได้!"));
        }

        room.getMemberFoodSelections().putIfAbsent(member, new LinkedList<>());
        LinkedList<String> selectedFoods = room.getMemberFoodSelections().get(member);

        if (selectedFoods.contains(foodType)) {
            selectedFoods.remove(foodType);
        } else {
            if (selectedFoods.size() >= room.getMaxFoodSelectionsPerMember()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "คุณเลือกครบจำนวนสูงสุดแล้ว กรุณายกเลิกอันเก่าก่อนเลือกใหม่!")
                );
            }
            selectedFoods.add(foodType);
        }

        // ✅ Broadcast ไปที่ Client ทุกคนในห้อง
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode + "/food-selection",
                Map.of("member", member, "selectedFoods", selectedFoods)
        );

        roomRepository.save(room);
        return ResponseEntity.ok(Map.of("member", member, "selectedFoods", selectedFoods));
    }

    @Transactional
    public ResponseEntity<String> setMemberReady(String roomCode, String memberName, boolean ready) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getMembers().contains(memberName)) {
            return ResponseEntity.badRequest().body("Member not found in the room!");
        }

        if (ready) {
            List<String> selectedFoods = room.getMemberFoodSelections().get(memberName);
            if (selectedFoods == null || selectedFoods.isEmpty()) {
                return ResponseEntity.badRequest().body("กรุณาเลือกอาหารอย่างน้อย 1 ประเภทก่อนกดพร้อม!");
            }
            if (selectedFoods.size() < room.getMaxFoodSelectionsPerMember()) {
                return ResponseEntity.badRequest().body("กรุณาเลือกอาหารให้ครบ " + room.getMaxFoodSelectionsPerMember() + " ประเภทก่อนกดพร้อม!");
            }
            room.getMemberReadyStatus().put(memberName, true);
        } else {
            room.getMemberReadyStatus().put(memberName, false);
            if (room.getMemberFoodSelections() != null) {
                room.getMemberFoodSelections().remove(memberName);
            }
        }

        roomRepository.save(room);
        messagingTemplate.convertAndSend("/topic/ready-status/" + roomCode, room.getMemberReadyStatus());

        return ResponseEntity.ok("Ready status updated successfully.");
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> randomFood(String roomCode, String ownerUser) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.getOwnerUser().equals(ownerUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only the owner can randomize food."));
        }

        if (room.getRandomizedAt() != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Food has already been randomized."));
        }

        for (String member : room.getMembers()) {
            if (room.getMemberFoodSelections().getOrDefault(member, new LinkedList<>()).size() < room.getMaxFoodSelectionsPerMember()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Not all members have selected their food yet."));
            }
        }

        if (!room.isAllMembersReady()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not all members are ready."));
        }

        // ✅ ส่ง event แจ้งว่าเริ่มสุ่มแล้ว
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode + "/random-started",
                "start"
        );

        Map<String, Integer> foodFrequencyMap = new HashMap<>();
        for (LinkedList<String> selections : room.getMemberFoodSelections().values()) {
            for (String food : selections) {
                foodFrequencyMap.put(food, foodFrequencyMap.getOrDefault(food, 0) + 1);
            }
        }

        if (foodFrequencyMap.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No food types selected by members."));
        }

        List<String> weightedFoodList = new ArrayList<>();
        foodFrequencyMap.forEach((food, count) -> {
            for (int i = 0; i < count; i++) weightedFoodList.add(food);
        });

        Collections.shuffle(weightedFoodList);
        String randomFood = weightedFoodList.get(0);

        List<RestaurantInfo> restaurants = googleMapsService.findNearbyRestaurants(
                room.getLatitude(),
                room.getLongitude(),
                1000,
                randomFood
        );

        room.setRandomizedAt(LocalDateTime.now());
        roomRepository.save(room);

        messagingTemplate.convertAndSend(
                "/topic/room/" + roomCode + "/random-result",
                Map.of(
                        "food", randomFood,
                        "restaurants", restaurants
                )
        );

        return ResponseEntity.ok(Map.of(
                "randomFood", randomFood,
                "restaurants", restaurants
        ));
    }


}
