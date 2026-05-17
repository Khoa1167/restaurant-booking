package com.restaurant.review;

import com.restaurant.auth.User;
import com.restaurant.reservation.Reservation;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "reviews")
public class Review extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @ManyToOne
    @JoinColumn(name = "reservation_id")
    public Reservation reservation;

    @Column(nullable = false)
    public int rating; // 1-5

    public String comment;

    public String userName;

    public LocalDateTime createdAt = LocalDateTime.now();

    public static List<Review> findAllOrderByDate() {
        return list("ORDER BY createdAt DESC");
    }

    public static double getAverageRating() {
        return find("SELECT AVG(r.rating) FROM Review r")
                .project(Double.class)
                .firstResult();
    }
}