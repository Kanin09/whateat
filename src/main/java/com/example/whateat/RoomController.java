package com.example.whateat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    // ✅ สร้างห้องใหม่ (รหัสจะถูกสุ่มอัตโนมัติ)
    @PostMapping("/create")
    public Room createRoom(@RequestBody Room room) {
        return roomService.createRoom(room);
    }

    // ✅ ค้นหาห้องโดยใช้รหัสห้อง
    @GetMapping("/find/{roomCode}")
    public Optional<Room> getRoomByCode(@PathVariable String roomCode) {
        return roomService.getRoomByCode(roomCode);
    }

    // ✅ ให้สมาชิกเข้าร่วมห้องโดยใช้ roomCode และ memberName
    @PostMapping("/join/{roomCode}")
    public ResponseEntity<Room> joinRoom(@PathVariable String roomCode, @RequestParam String memberName) {
        Room updatedRoom = roomService.joinRoom(roomCode, memberName);
        return ResponseEntity.ok(updatedRoom);
    }


}
