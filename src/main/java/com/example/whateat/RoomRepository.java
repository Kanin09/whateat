package com.example.whateat;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends MongoRepository<Room, String> {

    Optional<Room> findByRoomCode(String roomCode);

    // ✅ ค้นหาห้องที่อยู่ใกล้พิกัด (latitude, longitude) ในระยะที่กำหนด
    @Query("{'location': { $nearSphere: { $geometry: { type: 'Point', coordinates: [?0, ?1] }, $maxDistance: ?2 } } }")
    List<Room> findNearbyRooms(double latitude, double longitude, double maxDistance);
}
