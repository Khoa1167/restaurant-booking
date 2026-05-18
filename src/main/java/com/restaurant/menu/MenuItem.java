package com.restaurant.menu;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "menu_items")
public class MenuItem extends PanacheEntity {
    @Column(nullable = false)
    public String name;

    public String description;

    @Column(nullable = false)
    public Double price;

    // Các loại món ăn: "APPETIZER", "MAIN", "DESSERT", "DRINK"
    public String category;

    public String imageUrl;

    public Boolean available = true;
}