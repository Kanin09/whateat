package com.example.whateat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoogleMapsService {

    @Value("${google.maps.api.key}")
    private String googleApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public List<RestaurantInfo> findNearbyRestaurants(double latitude, double longitude, int radius, String foodTypeName) {
        String keyword = FoodType.fromDisplayName(foodTypeName)
                .map(FoodType::getKeyword)
                .orElse(foodTypeName);

        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("location", latitude + "," + longitude)
                .queryParam("rankby", "distance")
                .queryParam("keyword", keyword)
                .queryParam("type", "restaurant")
                .queryParam("key", googleApiKey)
                .toUriString();

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");

        return results.stream()
                .map(r -> RestaurantInfo.fromMap(r, googleApiKey))
                .collect(Collectors.toList());
    }

}
