package com.example.whateat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final Map<String, String> FOOD_TYPE_KEYWORD_MAP = Map.ofEntries(
            Map.entry("ของหวาน", "dessert"),
            Map.entry("อาหารตามสั่ง", "thai food"),
            Map.entry("อาหารจานเดียว", "one dish meal"),
            Map.entry("ก๋วยเตี๋ยว", "noodles"),
            Map.entry("เครื่องดื่ม/น้ำผลไม้", "juice"),
            Map.entry("เบเกอรี/เค้ก", "bakery"),
            Map.entry("ชาบู", "shabu"),
            Map.entry("อาหารเกาหลี", "korean"),
            Map.entry("ปิ้งย่าง", "grilled"),
            Map.entry("คาเฟ่", "cafe"),
            Map.entry("บุฟเฟ่ต์", "buffet")

    );

    public List<Map<String, String>> findNearbyRestaurants(double latitude, double longitude, int radius, String foodType) {
        String keyword = FOOD_TYPE_KEYWORD_MAP.getOrDefault(foodType, foodType);

        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("location", latitude + "," + longitude)
                .queryParam("radius", radius)
                .queryParam("type", "restaurant")
                //.queryParam("keyword", keyword)
                .queryParam("key", googleApiKey)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        List<Map<String, String>> restaurantList = new ArrayList<>();

        for (Map<String, Object> restaurant : results) {
            Map<String, String> data = new HashMap<>();
            data.put("name", (String) restaurant.get("name"));

            // ดึง types
            List<String> types = (List<String>) restaurant.get("types");
            if (types != null && !types.isEmpty()) {
                data.put("types", String.join(", ", types));
            } else {
                data.put("types", "No types available");
            }

            // ดึงรูป
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

            // ดึงพิกัด
            Map<String, Object> geometry = (Map<String, Object>) restaurant.get("geometry");
            Map<String, Object> location = (Map<String, Object>) geometry.get("location");
            String coordinates = location.get("lat") + "," + location.get("lng");
            data.put("location", coordinates);

            restaurantList.add(data);
        }

        return restaurantList;
    }

    // 🎯 [NEW!] Method ที่รวม types ทั้งหมดในระยะ 1 กิโล
    public Set<String> getAllTypesNearby(double latitude, double longitude, int radius) {
        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("location", latitude + "," + longitude)
                .queryParam("radius", radius)
                .queryParam("type", "restaurant")
                .queryParam("key", googleApiKey)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        Set<String> typeSet = new HashSet<>();

        for (Map<String, Object> restaurant : results) {
            List<String> types = (List<String>) restaurant.get("types");
            if (types != null) {
                typeSet.addAll(types);
            }
        }

        return typeSet; // ✅ return เป็น Set<String> (ไม่ซ้ำกัน)
    }
}
