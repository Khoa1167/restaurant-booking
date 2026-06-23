package com.restaurant.admin;

import com.restaurant.reservation.Reservation;
import com.restaurant.review.Review;
import com.restaurant.table.RestaurantTable;
import com.restaurant.auth.User;
import com.restaurant.menu.MenuItem;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @GET
    @Path("/stats")
    public Response getStats() {
        long totalReservations = Reservation.count();
        long pendingReservations = Reservation.count("status", "PENDING");
        long confirmedReservations = Reservation.count("status", "CONFIRMED");
        long cancelledReservations = Reservation.count("status", "CANCELLED");

        long totalTables = RestaurantTable.count();
        long availableTables = RestaurantTable.count("status", "AVAILABLE");
        long occupiedTables = RestaurantTable.count("status", "OCCUPIED");

        long totalUsers = User.count();
        long totalReviews = Review.count();
        long totalDishes = MenuItem.count();
        long popularDishes = MenuItem.count("popular", true);

        // Đánh giá trung bình
        Double avgRating = 0.0;
        List<Review> reviews = Review.listAll();
        if (reviews != null && !reviews.isEmpty()) {
            double sum = reviews.stream().mapToDouble(r -> r.rating).sum();
            avgRating = sum / reviews.size();
            avgRating = Math.round(avgRating * 10.0) / 10.0;
        }

        // Đặt bàn theo tháng
        int currentYear = java.time.LocalDate.now().getYear();
        List<Reservation> allReservations = Reservation.listAll();
        int[] monthlyCounts = new int[12];
        if (allReservations != null) {
            for (Reservation r : allReservations) {
                if (r.reservationTime != null && r.reservationTime.getYear() == currentYear) {
                    int month = r.reservationTime.getMonthValue();
                    if (month >= 1 && month <= 12) {
                        monthlyCounts[month - 1]++;
                    }
                }
            }
        }
        List<Integer> monthlyReservations = Arrays.stream(monthlyCounts)
                .boxed().collect(Collectors.toList());

        // Fix: lấy 5 đặt bàn mới nhất - dùng sort đúng cú pháp Panache
        List<Reservation> recentReservations = Reservation
                .find("id > 0 ORDER BY id DESC")
                .page(io.quarkus.panache.common.Page.ofSize(5))
                .list();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReservations",    totalReservations);
        stats.put("pendingReservations",  pendingReservations);
        stats.put("confirmedReservations",confirmedReservations);
        stats.put("cancelledReservations",cancelledReservations);
        stats.put("totalTables",          totalTables);
        stats.put("availableTables",      availableTables);
        stats.put("occupiedTables",       occupiedTables);
        stats.put("totalUsers",           totalUsers);
        stats.put("totalReviews",         totalReviews);
        stats.put("totalDishes",          totalDishes);
        stats.put("popularDishes",        popularDishes);
        stats.put("avgRating",            avgRating);
        stats.put("monthlyReservations",  monthlyReservations);
        stats.put("recentReservations",   recentReservations);
        return Response.ok(stats).build();
    }

    @GET
    @Path("/users")
    public Response getAllUsers() {
        return Response.ok(User.listAll()).build();
    }

    @GET
    @Path("/reviews")
    public Response getAllReviews() {
        return Response.ok(Review.listAll()).build();
    }
}