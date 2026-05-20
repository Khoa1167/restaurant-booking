package com.restaurant.menu;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "menu_items")
public class MenuItem extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String category;

    public String description;

    @Column(nullable = false)
    public String price;

    public String icon;

    public boolean popular = false;

    public boolean available = true;

    public static List<MenuItem> findAvailable() {
        return list("available", true);
    }

    public static List<MenuItem> findByCategory(String category) {
        return list("category = ?1 and available = true", category);
    }
}