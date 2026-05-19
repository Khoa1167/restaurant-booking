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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "table_id")
    public RestaurantTable table;

    @ManyToOne(fetch = FetchType.EAGER)
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

        return list("user.id = ?1 ORDER BY createdAt DESC", userId);
    }

    // Lấy tất cả theo trạng thái
    public static List<Reservation> findByStatus(String status) {
        return list("status", status);
    }

    public static boolean hasConflict(Long tableId, LocalDateTime time) {
        LocalDateTime from = time.minusHours(2);
        LocalDateTime to   = time.plusHours(2);
        return count(
                "table.id = ?1 AND status != 'CANCELLED' AND reservationTime BETWEEN ?2 AND ?3",
                tableId, from, to
        ) > 0;
    }
}