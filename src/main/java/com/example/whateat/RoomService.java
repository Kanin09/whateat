package com.example.whateat;


import jakarta.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;





    //สร้างห้อง
    @Transactional //รับจะไม่บันทึกข้อมูลลงในฐานข้อมูลถ้าไม่สมบูรณ์
    public Room createRoom(Room room) {
        if (room.getMaxUsers() <= 0) { // 🆕 ตรวจสอบว่าค่าที่ส่งมาต้องมากกว่า 0
            throw new IllegalArgumentException("Max users must be greater than 0.");
        }

        room.setRoomCode(room.getRoomCode() != null ? room.getRoomCode() : room.generateRoomCode());
        room.setMembers(new ArrayList<>());

        if (room.getOwnerUser() != null && !room.getOwnerUser().isEmpty()) {
            room.getMembers().add(room.getOwnerUser());  // เพิ่มเจ้าของเข้าไปในสมาชิก
        }

        if (room.getMaxFoodSelectionsPerMember() <= 0) {
            throw new ValidationException("Max food selections per member must be at least 1.");
        }


        room.setFoodTypes(new ArrayList<>(Room.DEFAULT_FOOD_TYPES));

        return roomRepository.save(room);
    }

    //  2. ฟังก์ชันหาห้องจากรหัสห้อง
    public Optional<Room> getRoomByCode(String roomCode) {
        return roomRepository.findByRoomCode(roomCode);  // ค้นหาห้องจาก MongoDB
    }


    //หัวห้องเท่านั้นที่ลบห้องได้
    @Transactional
    public ResponseEntity<String> deleteRoom(String roomCode, String ownerUser) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // ✅ ตรวจสอบว่าเป็นเจ้าของห้องหรือไม่
        if (!room.getOwnerUser().equals(ownerUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner can delete this room.");
        }

        // ✅ ลบห้องออกจากฐานข้อมูล
        roomRepository.delete(room);
        return ResponseEntity.ok("Room deleted successfully.");
    }


    @Transactional
    //ออกห้อง
    public ResponseEntity<String> leaveRoom(String roomCode, String memberName) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // ✅ ตรวจสอบว่าสมาชิกอยู่ในห้องหรือไม่
        if (!room.getMembers().contains(memberName)) {
            return ResponseEntity.badRequest().body("Member not found in the room!");
        }

        // ✅ ลบสมาชิกออกจากห้อง
        room.getMembers().remove(memberName);
        // ✅ ล้างข้อมูลการเลือกอาหารของสมาชิกที่ออกจากห้อง
        room.getMemberFoodSelections().remove(memberName);

        // ✅ ถ้าเจ้าของห้องออกจากห้อง ให้เปลี่ยนหัวหน้าห้องเป็นคนถัดไป
        if (room.getOwnerUser().equals(memberName)) {
            if (!room.getMembers().isEmpty()) {
                room.setOwnerUser(room.getMembers().get(0)); // กำหนดหัวหน้าห้องใหม่
            } else {
                roomRepository.delete(room); // ลบห้องถ้าไม่มีสมาชิกเหลือ
                return ResponseEntity.ok("Room deleted because no members left.");
            }
        }

        roomRepository.save(room);
        return ResponseEntity.ok("Member " + memberName + " has left the room.");
    }

    //เตะสมาชิกโดยหัวหน้า
    @Transactional
    public ResponseEntity<String> kickMember(String roomCode, String ownerUser, String memberName) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));


        // ✅ ตรวจสอบว่าเป็นเจ้าของห้องหรือไม่
        if (!room.getOwnerUser().equals(ownerUser)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only the owner can kick members.");
        }

        if (memberName == null || memberName.isEmpty()) {
            return ResponseEntity.badRequest().body("MemberName is required!");
        }

        // ✅ ตรวจสอบว่าสมาชิกอยู่ในห้องหรือไม่
        if (!room.getMembers().contains(memberName)) {
            return ResponseEntity.badRequest().body("Member not found in the room!");
        }

        // ❌ หัวหน้าห้อง **ไม่สามารถเตะตัวเองออกจากห้องได้**
        if (ownerUser.equals(memberName)) {
            return ResponseEntity.badRequest().body("Owner cannot kick themselves!");
        }


        // ✅ ลบสมาชิกออกจากห้อง
        room.getMembers().remove(memberName);
        // ✅ ล้างข้อมูลการเลือกอาหารของสมาชิกที่ถูกเตะออก
        room.getMemberFoodSelections().remove(memberName);
        roomRepository.save(room);

        return ResponseEntity.ok("Member " + memberName + " has been kicked from the room.");
    }




    // ฟังก์ชันให้ผู้ใช้เข้าร่วมห้องโดยใช้รหัสห้อง
    // ให้ผู้ใช้เข้าร่วมห้องโดยใช้รหัสห้องและเพิ่มชื่อสมาชิก
    @Transactional
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

        // บันทึกการเปลี่ยนแปลงลงฐานข้อมูล
        return roomRepository.save(room);
    }



    // เลือกประเภทอาหาร
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

        // ✅ ตรวจสอบว่าสมาชิกคนนี้เคยเลือกอาหารประเภทนี้ไปแล้วหรือยัง
        if (room.hasMemberSelectedFood(member, foodType)) {
            return ResponseEntity.badRequest().body("You have already selected this food type!");
        }

        // ✅ เช็คว่าสมาชิกเลือกอาหารครบจำนวนที่กำหนดหรือยัง
        if (!room.canSelectMoreFood(member)) {
            return ResponseEntity.badRequest().body("You have reached the max food selection limit!");
        }

        // ✅ บันทึกการเลือกอาหาร
        room.selectFood(member, foodType);
        roomRepository.save(room);

        return ResponseEntity.ok("Member " + member + " selected food type: " + foodType);
    }


}
