package com.restaurant.reservation;

import com.restaurant.table.RestaurantTable;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import com.restaurant.auth.User;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

//@Inject
//JsonWebToken jwt;

@Path("/api/reservations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class ReservationResource {
    @Inject
    JsonWebToken jwt;

    // Lấy tất cả (admin)
    @GET
    @RolesAllowed("ADMIN")
    public Response getAll(@QueryParam("status") String status) {
        List<Reservation> list = (status != null && !status.isBlank())
                ? Reservation.findByStatus(status)
                : Reservation.list("ORDER BY reservationTime DESC");
        return Response.ok(list).build();
    }
    // Tạo đặt bàn mới
    @POST
    @Transactional
    @RolesAllowed({"USER", "ADMIN"})
    public Response create(ReservationRequest req) {
        if (req.customerName == null || req.customerName.isBlank())
            return Response.status(400).entity(Map.of("message", "Vui lòng nhập tên người đặt")).build();
        if (req.phone == null || req.phone.isBlank())
            return Response.status(400).entity(Map.of("message", "Vui lòng nhập số điện thoại")).build();
        if (req.reservationTime == null || req.tableId == null)
            return Response.status(400).entity(Map.of("message", "Vui lòng chọn thời gian và bàn")).build();
        if (req.numberOfPeople <= 0)
            return Response.status(400).entity(Map.of("message", "Số người phải lớn hơn 0")).build();

        // Phải đặt trước ít nhất 30 phút
        if (req.reservationTime.isBefore(LocalDateTime.now().plusMinutes(30)))
            return Response.status(400).entity(Map.of("message", "Thời gian đặt bàn phải trước ít nhất 30 phút")).build();

        RestaurantTable table = RestaurantTable.findById(req.tableId);
        if (table == null)
            return Response.status(404).entity(Map.of("message", "Không tìm thấy bàn")).build();
        if ("OCCUPIED".equals(table.status))
            return Response.status(400).entity(Map.of("message", "Bàn này hiện đang có người")).build();

        // Validate sức chứa
        if (req.numberOfPeople > table.capacity)
            return Response.status(400).entity(Map.of("message", "Bàn chỉ chứa tối đa " + table.capacity + " người")).build();

        // Validate xung đột thời gian ±2 giờ
        if (Reservation.hasConflict(req.tableId, req.reservationTime))
            return Response.status(400).entity(Map.of("message", "Bàn đã được đặt vào khung giờ gần đó (±2 giờ)")).build();

        Reservation reservation = new Reservation();
        reservation.table           = table;
        reservation.customerName    = req.customerName.trim();
        reservation.phone           = req.phone.trim();
        reservation.email           = req.email;
        reservation.reservationTime = req.reservationTime;
        reservation.numberOfPeople  = req.numberOfPeople;
        reservation.note            = req.note;
        reservation.status          = "PENDING";

        // Gắn user từ JWT thay vì nhận từ client
        String subject = jwt.getSubject();
        if (subject != null) {
            try { reservation.user = User.findById(Long.parseLong(subject)); }
            catch (NumberFormatException ignored) {}
        }

        reservation.persist();
        table.status = "OCCUPIED";
        return Response.status(201).entity(reservation).build();
    }

    // Cập nhật trạng thái (admin)
    @PUT
    @Path("/{id}/status")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response updateStatus(@PathParam("id") Long id, StatusRequest req) {
        if (req.status == null || (!req.status.equals("CONFIRMED") && !req.status.equals("CANCELLED") && !req.status.equals("PENDING")))
            return Response.status(400).entity(Map.of("message", "Trạng thái không hợp lệ")).build();

        Reservation reservation = Reservation.findById(id);
        if (reservation == null)
            return Response.status(404).entity(Map.of("message", "Không tìm thấy đặt bàn")).build();

        reservation.status = req.status;

        if (reservation.table != null) {
            if ("CANCELLED".equals(req.status)) {
                // Chỉ trả bàn nếu không còn đặt bàn active nào khác
                long activeCount = Reservation.count(
                        "table.id = ?1 AND status IN ('PENDING', 'CONFIRMED') AND id != ?2",
                        reservation.table.id, id);
                if (activeCount == 0) reservation.table.status = "AVAILABLE";
            } else if ("CONFIRMED".equals(req.status)) {
                reservation.table.status = "OCCUPIED";
            }
        }

        return Response.ok(reservation).build();
    }

    // Huỷ đặt bàn (user)
    @PUT
    @Path("/{id}/cancel")
    @Transactional
    @RolesAllowed({"USER", "ADMIN"})
    public Response cancel(@PathParam("id") Long id) {
        Reservation reservation = Reservation.findById(id);
        if (reservation == null)
            return Response.status(404).entity(Map.of("message", "Không tìm thấy đặt bàn")).build();

        String subject = jwt.getSubject();
        boolean isAdmin = jwt.getGroups().contains("ADMIN");

        // Chỉ được huỷ đặt bàn của chính mình (trừ admin)
        if (!isAdmin && (reservation.user == null || !String.valueOf(reservation.user.id).equals(subject)))
            return Response.status(403).entity(Map.of("message", "Bạn không có quyền huỷ đặt bàn này")).build();

        if ("CANCELLED".equals(reservation.status))
            return Response.status(400).entity(Map.of("message", "Đặt bàn này đã được huỷ trước đó")).build();
        if ("CONFIRMED".equals(reservation.status) && !isAdmin)
            return Response.status(400).entity(Map.of("message", "Không thể huỷ đặt bàn đã xác nhận. Vui lòng liên hệ nhà hàng")).build();

        reservation.status = "CANCELLED";

        // Chỉ trả bàn về AVAILABLE nếu không còn đặt bàn active nào khác tại bàn đó
        if (reservation.table != null) {
            long activeCount = Reservation.count(
                    "table.id = ?1 AND status IN ('PENDING', 'CONFIRMED') AND id != ?2",
                    reservation.table.id, id);
            if (activeCount == 0) reservation.table.status = "AVAILABLE";
        }

        return Response.ok(Map.of("message", "Huỷ đặt bàn thành công")).build();
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
    }

    public static class StatusRequest {
        public String status;
    }
}