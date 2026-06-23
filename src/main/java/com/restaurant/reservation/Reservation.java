package com.restaurant.reservation;

import com.restaurant.auth.User;
import com.restaurant.table.RestaurantTable;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

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

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    public List<ReservationItem> items = new ArrayList<>();

    @Column(nullable = false)
    public String status = "PENDING";

    public LocalDateTime createdAt = LocalDateTime.now();

    public static List<Reservation> findByUserId(Long userId) {
        return list("user.id", userId);
    }

    public static List<Reservation> findByStatus(String status) {
        return list("status", status);
    }
}