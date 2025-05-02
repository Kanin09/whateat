package com.example.whateat;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private  RoomRepository roomRepository;

    private final GoogleMapsService googleMapsService;

    @Autowired
    public RoomController(GoogleMapsService googleMapsService){

        this.googleMapsService = googleMapsService;
    }

    // ✅ สร้างห้องใหม่ (รหัสจะถูกสุ่มอัตโนมัติ)
    @PostMapping("/create")
    public Room createRoom(@Valid @RequestBody Room room){

        return roomService.createRoom(room);
    }

    // ✅ ดึงข้อมูลห้องตาม roomCode
    @GetMapping("/{roomCode}")
    public ResponseEntity<Room> getRoomInfo(@PathVariable String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        return ResponseEntity.ok(room); // ✅ มี ownerUser อยู่ใน Room อยู่แล้ว ไม่ต้องสร้าง DTO
    }


    // ✅ หัวหน้าห้องลบห้อง
    @DeleteMapping("/delete/{roomCode}")
    public ResponseEntity<String> deleteRoom(@PathVariable String roomCode,
                                             @RequestParam String ownerUser) {
        return roomService.deleteRoom(roomCode, ownerUser);
    }


    // ✅ สมาชิกออกจากห้อง
    @PostMapping("/leave/{roomCode}")
    public ResponseEntity<String> leaveRoom(@PathVariable String roomCode,
                                            @RequestParam String memberName) {
        return roomService.leaveRoom(roomCode, memberName);
    }


    // ✅ เตะสมาชิกออกห้องเฉพาะหัวหน้า
    @PostMapping("/kick/{roomCode}")
    public ResponseEntity<String> kickMember(@PathVariable String roomCode,
                                             @RequestParam String ownerUser,
                                             @RequestParam String memberName) {
        return roomService.kickMember(roomCode, ownerUser, memberName);
    }

    // ✅ ให้สมาชิกเข้าร่วมห้องโดยใช้ roomCode และ memberName
    @PostMapping("/join/{roomCode}")
    public ResponseEntity<Room> joinRoom(@PathVariable String roomCode, @RequestParam String memberName) {
        Room updatedRoom = roomService.joinRoom(roomCode, memberName);
        return ResponseEntity.ok(updatedRoom);
    }

    // เลือกประเภทอาหาร type

    @PostMapping("/selectFood/{roomCode}")
    public ResponseEntity<?> selectFood(@PathVariable String roomCode, @RequestParam String member, @RequestParam String foodType) {
        return roomService.selectFood(roomCode, member, foodType);
    }


    // ✅ สุ่มอาหาร 1 อย่าง (Owner เท่านั้น)
    @PostMapping("/randomFood/{roomCode}")
    public ResponseEntity<Map<String, Object>> randomFood(@PathVariable String roomCode, @RequestParam String ownerUser) {
        return roomService.randomFood(roomCode, ownerUser);
    }

    @PostMapping("/ready/{roomCode}")
    public ResponseEntity<String> setMemberReady(
            @PathVariable String roomCode,
            @RequestParam String memberName,
            @RequestParam boolean ready
    ) {
        return roomService.setMemberReady(roomCode, memberName, ready);
    }


}
