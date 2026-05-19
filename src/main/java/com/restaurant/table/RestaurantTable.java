package com.restaurant.table;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public int capacity;

    @Column(nullable = false)
    public String status = "AVAILABLE"; // AVAILABLE, OCCUPIED

    public String description;

    public LocalDateTime createdAt = LocalDateTime.now();

    public static List<RestaurantTable> findAvailable() {
        return list("status", "AVAILABLE");
    }

    // Thêm query mới vào trong class, sau findAvailable():
    public static List<RestaurantTable> findByCapacity(int minCapacity) {
        return list("capacity >= ?1 AND status = 'AVAILABLE'", minCapacity);
    }
}