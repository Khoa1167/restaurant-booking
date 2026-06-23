package com.restaurant.reservation;

import com.restaurant.menu.MenuItem;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "reservation_items")
public class ReservationItem extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    @JsonIgnore
    public Reservation reservation;

    @ManyToOne
    @JoinColumn(name = "menu_item_id", nullable = false)
    public MenuItem menuItem;

    @Column(nullable = false)
    public int quantity;
}
