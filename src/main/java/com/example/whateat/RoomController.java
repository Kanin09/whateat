package com.example.whateat;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // ✅ สร้างห้องใหม่ (รหัสจะถูกสุ่มอัตโนมัติ)
    @PostMapping("/create")
    public Room createRoom(@Valid @RequestBody Room room) {
        return roomService.createRoom(room);
    }


    // ✅ ค้นหาห้องโดยใช้รหัสห้อง
    @GetMapping("/find/{roomCode}")
    public Optional<Room> getRoomByCode(@PathVariable String roomCode) {
        return roomService.getRoomByCode(roomCode);
    }

    // ✅ หัวหน้าห้องลบห้อง
    @DeleteMapping("/delete/{roomCode}")
    public ResponseEntity<String> deleteRoom(@PathVariable String roomCode, @RequestParam String ownerUser) {
        return roomService.deleteRoom(roomCode, ownerUser);
    }


    // ✅ สมาชิกออกจากห้อง
    @PostMapping("/leave/{roomCode}")
    public ResponseEntity<String> leaveRoom(@PathVariable String roomCode, @RequestParam String memberName) {
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
    public ResponseEntity<String> randomFood(@PathVariable String roomCode, @RequestParam String ownerUser) {
        return roomService.randomFood(roomCode, ownerUser);
    }



}
