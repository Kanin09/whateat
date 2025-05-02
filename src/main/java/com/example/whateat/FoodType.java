package com.example.whateat;

import java.util.Arrays;
import java.util.Optional;

public enum FoodType {

    DESSERT("ของหวาน", "dessert"),
    THAI_FOOD("อาหารตามสั่ง", "Thai street food"),
    ONE_DISH("อาหารจานเดียว", "Thai street food"),
    NOODLE("ก๋วยเตี๋ยว", "noodle"),
    JUICE("เครื่องดื่ม/น้ำผลไม้", "juice"),
    BAKERY("เบเกอรี/เค้ก", "bakery"),
    SHABU("ชาบู", "shabu"),
    KOREAN("อาหารเกาหลี", "korean food"),
    GRILL("ปิ้งย่าง", "grill"),
    CAFE("คาเฟ่", "cafe"),
    BUFFET("บุฟเฟ่ต์", "buffet");

    private final String displayName;
    private final String keyword;

    FoodType(String displayName, String keyword) {
        this.displayName = displayName;
        this.keyword = keyword;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKeyword() {
        return keyword;
    }

    // 🔍 ใช้เมธอดนี้หา Enum จากชื่อไทยที่ผู้ใช้เลือก
    public static Optional<FoodType> fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(type -> type.displayName.equalsIgnoreCase(displayName))
                .findFirst();
    }
}
