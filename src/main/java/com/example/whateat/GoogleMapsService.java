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

    // ✅ Map สำหรับแปลงชื่อไทย → คำค้น Google
    private static final Map<String, String> FOOD_TYPE_KEYWORD_MAP = Map.ofEntries(
            Map.entry("ของหวาน", "dessert"),
            Map.entry("อาหารตามสั่ง", "thai food"),
            Map.entry("อาหารจานเดียว", "one dish meal"),
            Map.entry("ก๋วยเตี๋ยว", "noodles"),
            Map.entry("เครื่องดื่ม/น้ำผลไม้", "juice drinks"),
            Map.entry("เบเกอรี/เค้ก", "bakery"),
            Map.entry("ชาบู", "shabu"),
            Map.entry("อาหารเกาหลี", "korean food"),
            Map.entry("ปิ้งย่าง", "grilled meat")
    );

    // 🔍 ค้นหาร้านอาหารใกล้พิกัดที่กำหนด
    public List<Map<String, String>> findNearbyRestaurants(double latitude, double longitude, int radius, String foodType) {
        // ✅ แปลงประเภทอาหาร ถ้าไม่มีใน Map ให้ใช้ค่าที่รับมา
        String keyword = FOOD_TYPE_KEYWORD_MAP.getOrDefault(foodType, foodType);

        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("location", latitude + "," + longitude)
                .queryParam("radius", radius)
                .queryParam("type", "restaurant")
                .queryParam("keyword", keyword)
                .queryParam("key", googleApiKey)
                .toUriString();

        // 📌 เรียก API และรับข้อมูล JSON
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
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
