package com.restaurant.reservation;

import com.restaurant.auth.User;
import com.restaurant.table.RestaurantTable;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "table_id")
    public RestaurantTable table;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @Column(nullable = false)
    public String customerName;

    @Column(nullable = false)
    public String phone;

    public String email;

    @Column(nullable = false)
    public LocalDateTime reservationTime;

    @Column(nullable = false)
    public int numberOfPeople;

    public String note;

    @Column(nullable = false)
    public String status = "PENDING"; // PENDING, CONFIRMED, CANCELLED

    public LocalDateTime createdAt = LocalDateTime.now();

    // Lấy reservation theo user
    public static List<Reservation> findByUserId(Long userId) {
        return list("user.id", userId);
    }

    // Lấy tất cả theo trạng thái
    public static List<Reservation> findByStatus(String status) {
        return list("status", status);
    }
}