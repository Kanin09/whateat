package com.example.whateat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔍 ค้นหาร้านอาหารใกล้พิกัดที่กำหนด
    public List<Map<String, String>> findNearbyRestaurants(double latitude, double longitude, int radius, String foodType) {
        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("location", latitude + "," + longitude)
                .queryParam("radius", radius)
                .queryParam("type", "restaurant")
                .queryParam("keyword", foodType)
                .queryParam("key", googleApiKey)
                .toUriString();

        // 📌 เรียก API และรับข้อมูล JSON
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        // 📌 ดึงเฉพาะข้อมูลที่ต้องการ (ชื่อ, รูป, พิกัด)
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        List<Map<String, String>> restaurantList = new ArrayList<>();

        for (Map<String, Object> restaurant : results) {
            Map<String, String> data = new HashMap<>();
            data.put("name", (String) restaurant.get("name"));

            // 📌 ดึง URL รูปภาพ
            if (restaurant.containsKey("photos")) {
                List<Map<String, Object>> photos = (List<Map<String, Object>>) restaurant.get("photos");
                String photoRef = (String) photos.get(0).get("photo_reference");
                String photoUrl = "https://maps.googleapis.com/maps/api/place/photo"
                        + "?maxwidth=400"
                        + "&photo_reference=" + photoRef
                        + "&key=" + googleApiKey;
                data.put("image", photoUrl);
            } else {
                data.put("image", "No image available");
            }

            // 📌 ดึงโลเคชั่น
            Map<String, Object> geometry = (Map<String, Object>) restaurant.get("geometry");
            Map<String, Object> location = (Map<String, Object>) geometry.get("location");
            String coordinates = location.get("lat") + "," + location.get("lng");
            data.put("location", coordinates);

            restaurantList.add(data);
        }

        return restaurantList;
    }

}
