package com.restaurant.auth;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(unique = true, nullable = false)
    public String email;

    public String phone;

    @Column(nullable = false)
    public String password;

    @Column(nullable = false)
    public String role = "USER";

    public LocalDateTime createdAt = LocalDateTime.now();

    public static User findByEmail(String email) {
        return find("email", email).firstResult();
    }
}