package com.restaurant.reservation;

import com.restaurant.auth.User;
import com.restaurant.table.RestaurantTable;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import com.restaurant.menu.MenuItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Path("/api/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReservationResource {

    @Inject
    JsonWebToken jwt;

    // Lấy tất cả (admin)
    @GET
    public List<Reservation> getAll() {
        return Reservation.listAll();
    }

    // Lấy theo user hiện tại
    @GET
    @Path("/my")
    public List<Reservation> getMy(@QueryParam("userId") Long userId) {
        if (userId == null) {
            try {
                String subject = jwt.getSubject();
                if (subject != null) {
                    userId = Long.parseLong(subject);
                }
            } catch (Exception ignored) {}
        }
        if (userId != null) {
            return Reservation.findByUserId(userId);
        }
        return List.of();
    }

    // Tạo đặt bàn mới
    @POST
    @Transactional
    public Response create(ReservationRequest req) {
        if (req.customerName == null || req.phone == null || req.reservationTime == null) {
            return Response.status(400)
                    .entity(Map.of("message", "Vui long dien day du thong tin"))
                    .build();
        }
        if (req.reservationTime.isBefore(LocalDateTime.now())) {
            return Response.status(400)
                    .entity(Map.of("message", "Thoi gian dat ban khong hop le"))
                    .build();
        }

        RestaurantTable table = RestaurantTable.findById(req.tableId);
        if (table == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay ban"))
                    .build();
        }
        if ("OCCUPIED".equals(table.status)) {
            return Response.status(400)
                    .entity(Map.of("message", "Ban nay da co nguoi"))
                    .build();
        }

        Reservation reservation = new Reservation();
        reservation.table           = table;
        reservation.customerName    = req.customerName;
        reservation.phone           = req.phone;
        reservation.email           = req.email;
        reservation.reservationTime = req.reservationTime;
        reservation.numberOfPeople  = req.numberOfPeople;
        reservation.note            = req.note;
        reservation.status          = "PENDING";

        // Tự động gán user từ JWT token
        try {
            String subject = jwt.getSubject();
            if (subject != null) {
                User user = User.findById(Long.parseLong(subject));
                reservation.user = user;
            }
        } catch (Exception ignored) {}

        // Xử lý lưu các món ăn đặt kèm
        if (req.orderItems != null) {
            for (OrderItemRequest itemReq : req.orderItems) {
                if (itemReq.menuItemId != null && itemReq.quantity > 0) {
                    MenuItem menuItem = MenuItem.findById(itemReq.menuItemId);
                    if (menuItem != null) {
                        ReservationItem resItem = new ReservationItem();
                        resItem.reservation = reservation;
                        resItem.menuItem = menuItem;
                        resItem.quantity = itemReq.quantity;
                        reservation.items.add(resItem);
                    }
                }
            }
        }

        reservation.persist();
        table.status = "OCCUPIED";

        return Response.ok(reservation).status(201).build();
    }

    // Cập nhật trạng thái (admin)
    @PUT
    @Path("/{id}/status")
    @Transactional
    public Response updateStatus(@PathParam("id") Long id, StatusRequest req) {
        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay dat ban"))
                    .build();
        }
        reservation.status = req.status;
        if ("CANCELLED".equals(req.status) && reservation.table != null) {
            reservation.table.status = "AVAILABLE";
        }
        return Response.ok(reservation).build();
    }

    // Huỷ đặt bàn (user)
    @PUT
    @Path("/{id}/cancel")
    @Transactional
    public Response cancel(@PathParam("id") Long id) {
        Reservation reservation = Reservation.findById(id);
        if (reservation == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay dat ban"))
                    .build();
        }
        if ("CONFIRMED".equals(reservation.status)) {
            return Response.status(400)
                    .entity(Map.of("message", "Khong the huy dat ban da xac nhan"))
                    .build();
        }
        reservation.status = "CANCELLED";
        if (reservation.table != null) {
            reservation.table.status = "AVAILABLE";
        }
        return Response.ok(Map.of("message", "Huy dat ban thanh cong")).build();
    }

    // ── DTOs ─────────────────────────────────────
    public static class ReservationRequest {
        public Long tableId;
        public String customerName;
        public String phone;
        public String email;
        public LocalDateTime reservationTime;
        public int numberOfPeople;
        public String note;
        public List<OrderItemRequest> orderItems;
    }

    public static class OrderItemRequest {
        public Long menuItemId;
        public int quantity;
    }

    public static class StatusRequest {
        public String status;
    }
}