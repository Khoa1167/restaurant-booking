package com.restaurant.review;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.restaurant.auth.User;
import com.restaurant.reservation.Reservation;

@Entity
@Table(name = "reviews")
public class Review extends PanacheEntity {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    public Reservation reservation;

    @Column(nullable = false)
    public Integer rating;      // Điểm đánh giá từ 1 đến 5

    @Column(columnDefinition = "TEXT")
    public String comment;

    public LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}