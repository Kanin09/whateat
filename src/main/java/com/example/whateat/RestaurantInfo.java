package com.example.whateat;

import java.util.List;
import java.util.Map;

public class RestaurantInfo {

    private String name;
    private String address;     // formatted_address หรือ vicinity
    private String types;       // joined string from types array
    private Double rating;      // rating อาจเป็น null
    private String imageUrl;    // จาก photo_reference
    private String location;    // lat,lng

    // Constructors
    public RestaurantInfo() {}


    public RestaurantInfo(String name, String address, String types, Double rating, String imageUrl, String location) {
        this.name = name;
        this.address = address;
        this.types = types;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.location = location;
    }

    @SuppressWarnings("unchecked")
    public static RestaurantInfo fromMap(Map<String, Object> restaurant, String googleApiKey) {
        String name = (String) restaurant.getOrDefault("name", "No name available");

        // รองรับทั้ง "vicinity" และ "formatted_address"
        String address = (String) restaurant.getOrDefault("vicinity",
                restaurant.getOrDefault("formatted_address", "No address available"));

        List<String> types = (List<String>) restaurant.get("types");
        String typesStr = types != null ? String.join(", ", types) : "No types available";

        Double rating = null;
        if (restaurant.containsKey("rating")) {
            try {
                rating = ((Number) restaurant.get("rating")).doubleValue();
            } catch (Exception ignored) {}
        }

        // ดึงรูปจาก photo_reference
        String imageUrl = "No image available";
        if (restaurant.containsKey("photos")) {
            List<Map<String, Object>> photos = (List<Map<String, Object>>) restaurant.get("photos");
            if (!photos.isEmpty() && photos.get(0).containsKey("photo_reference")) {
                String photoRef = (String) photos.get(0).get("photo_reference");
                imageUrl = "https://maps.googleapis.com/maps/api/place/photo"
                        + "?maxwidth=400"
                        + "&photo_reference=" + photoRef
                        + "&key=" + googleApiKey;
            }
        }

        String coordinates = "Unknown";
        try {
            Map<String, Object> geometry = (Map<String, Object>) restaurant.get("geometry");
            Map<String, Object> location = (Map<String, Object>) geometry.get("location");
            coordinates = location.get("lat") + "," + location.get("lng");
        } catch (Exception e) {
            // handle gracefully
        }

        return new RestaurantInfo(name, address, typesStr, rating, imageUrl, coordinates);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
