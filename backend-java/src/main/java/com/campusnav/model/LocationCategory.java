package com.campusnav.model;

public enum LocationCategory {
    ENTRY("Entry & Gates", "gate"),
    ACADEMIC("Academic Buildings", "school"),
    LABORATORY("Laboratories & Workshops", "flask"),
    ADMINISTRATION("Administration", "building"),
    LIBRARY("Library & Digital Resources", "book-open"),
    FOOD("Canteen & Dining", "utensils"),
    HOSTEL("Student Residences", "home"),
    SPORTS("Sports & Athletics", "trophy"),
    HEALTH("Medical & Healthcare", "heart-pulse"),
    AMENITIES("Auditoriums & Activity Centers", "users"),
    PARKING("Parking Facilities", "car"),
    OTHER("Campus Amenities", "map-pin");

    private final String displayName;
    private final String iconName;

    LocationCategory(String displayName, String iconName) {
        this.displayName = displayName;
        this.iconName = iconName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconName() {
        return iconName;
    }
}
