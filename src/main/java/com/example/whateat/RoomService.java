package com.example.whateat;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;





    //สร้างห้อง
    public Room createRoom(Room room) {
        if (room.getMaxUsers() <= 0) { // 🆕 ตรวจสอบว่าค่าที่ส่งมาต้องมากกว่า 0
            throw new IllegalArgumentException("Max users must be greater than 0.");
        }

        room.setRoomCode(room.getRoomCode() != null ? room.getRoomCode() : room.generateRoomCode());
        room.setMembers(new ArrayList<>());


        return roomRepository.save(room);
    }

    //  2. ฟังก์ชันหาห้องจากรหัสห้อง
    public Optional<Room> getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode);  // ค้นหาห้องจาก MongoDB
    }

    //  3. ฟังก์ชันให้ผู้ใช้เข้าร่วมห้องโดยใช้รหัสห้อง



    // ให้ผู้ใช้เข้าร่วมห้องโดยใช้รหัสห้องและเพิ่มชื่อสมาชิก
    public Room joinRoom(String roomCode, String memberName) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (!room.canJoin()) {
            throw new RuntimeException("Room is full!");
        }

        // ตรวจสอบว่าผู้ใช้ได้อยู่ในห้องแล้วหรือไม่
        if (room.getMembers().contains(memberName)) {
            throw new RuntimeException("Member already in the room!");
        }

        // เพิ่มสมาชิกใหม่ลงในห้อง
        room.getMembers().add(memberName);
        //นับจำนวนสมาชิกจาก type countUser จาก member
        room.setCountUsers(room.getMembers().size());



        // บันทึกการเปลี่ยนแปลงลงฐานข้อมูล
        return roomRepository.save(room);
    }
}
