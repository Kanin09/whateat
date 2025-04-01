package com.example.whateat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class RoomCleanupService {

    @Autowired
    private RoomRepository roomRepository;

    @Scheduled(fixedRate = 60000) // ทำงานทุก 1 นาที
    public void deleteExpiredRooms() {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minus(2, ChronoUnit.HOURS);

        List<Room> expiredRooms = roomRepository.findAll().stream()
                .filter(room -> room.getRandomizedAt() != null && room.getRandomizedAt().isBefore(twoHoursAgo))
                .toList();

        if (!expiredRooms.isEmpty()) {
            roomRepository.deleteAll(expiredRooms);
            System.out.println("Deleted " + expiredRooms.size() + " expired rooms.");
        }

    }
}
