package com.restaurant.table;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.annotation.security.RolesAllowed;
import java.util.List;
import java.util.Map;

@Path("/api/tables")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TableResource {

    // Lấy tất cả bàn
    @GET
    public List<RestaurantTable> getAll() {
        return RestaurantTable.listAll();
    }

    // Lấy bàn trống
    @GET
    @Path("/available")
    public List<RestaurantTable> getAvailable() {
        return RestaurantTable.findAvailable();
    }

    // Tạo bàn mới (admin)
    @POST
    @Transactional
    @RolesAllowed("ADMIN")
    public Response create(RestaurantTable table) {
        if (table.name == null || table.name.isBlank()) {
            return Response.status(400)
                    .entity(Map.of("message", "Tên bàn không được để trống"))
                    .build();
        }
        if (table.capacity <= 0) {
            return Response.status(400)
                    .entity(Map.of("message", "Sức chứa phải lớn hơn 0"))
                    .build();
        }
        // Kiểm tra trùng tên
        if (RestaurantTable.count("name = ?1", table.name.trim()) > 0) {
            return Response.status(400)
                    .entity(Map.of("message", "Tên bàn đã tồn tại"))
                    .build();
        }
        table.status = "AVAILABLE";
        table.persist();
        return Response.status(201).entity(table).build();
    }

    // Cập nhật trạng thái bàn
    @PUT
    @Path("/{id}/status")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response updateStatus(@PathParam("id") Long id, RestaurantTable body) {
        if (body.status == null ||
                (!body.status.equals("AVAILABLE") && !body.status.equals("OCCUPIED"))) {
            return Response.status(400)
                    .entity(Map.of("message", "Trạng thái không hợp lệ. Chỉ chấp nhận AVAILABLE hoặc OCCUPIED"))
                    .build();
        }
        RestaurantTable table = RestaurantTable.findById(id);
        if (table == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Không tìm thấy bàn"))
                    .build();
        }
        table.status = body.status;
        return Response.ok(table).build();
    }

    // Xóa bàn (admin)
    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed("ADMIN")
    public Response delete(@PathParam("id") Long id) {
        RestaurantTable table = RestaurantTable.findById(id);
        if (table == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Không tìm thấy bàn"))
                    .build();
        }
        if ("OCCUPIED".equals(table.status)) {
            return Response.status(400)
                    .entity(Map.of("message", "Không thể xoá bàn đang được sử dụng"))
                    .build();
        }
        table.delete();
        return Response.noContent().build();
    }
}