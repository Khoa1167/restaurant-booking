package com.restaurant.admin;

import com.restaurant.reservation.Reservation;
import com.restaurant.review.Review;
import com.restaurant.table.RestaurantTable;
import com.restaurant.auth.User;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;

@Path("/api/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    // Thống kê tổng quan
    @GET
    @Path("/stats")
    public Response getStats() {
        long totalReservations = Reservation.count();
        long pendingReservations = Reservation.count("status", "PENDING");
        long confirmedReservations = Reservation.count("status", "CONFIRMED");
        long cancelledReservations = Reservation.count("status", "CANCELLED");
        long totalTables = RestaurantTable.count();
        long availableTables = RestaurantTable.count("status", "AVAILABLE");
        long totalUsers = User.count();
        long totalReviews = Review.count();

        return Response.ok(Map.of(
                "totalReservations",    totalReservations,
                "pendingReservations",  pendingReservations,
                "confirmedReservations",confirmedReservations,
                "cancelledReservations",cancelledReservations,
                "totalTables",          totalTables,
                "availableTables",      availableTables,
                "totalUsers",           totalUsers,
                "totalReviews",         totalReviews
        )).build();
    }

    // Lấy tất cả user (admin)
    @GET
    @Path("/users")
    public Response getAllUsers() {
        return Response.ok(User.listAll()).build();
    }

    // Lấy tất cả review (admin)
    @GET
    @Path("/reviews")
    public Response getAllReviews() {
        return Response.ok(Review.listAll()).build();
    }
}