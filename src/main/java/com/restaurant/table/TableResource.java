package com.restaurant.table;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
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

    // Lấy chi tiết bàn ăn
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id) {
        RestaurantTable table = RestaurantTable.findById(id);
        if (table == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay ban"))
                    .build();
        }
        return Response.ok(table).build();
    }

    // Tạo bàn mới (admin)
    @POST
    @Transactional
    public Response create(RestaurantTable table) {
        if (table.name == null || table.capacity <= 0) {
            return Response.status(400)
                    .entity(Map.of("message", "Ten ban va suc chua khong hop le"))
                    .build();
        }
        if (table.status == null) {
            table.status = "AVAILABLE";
        }
        table.persist();
        return Response.ok(table).status(201).build();
    }

    // Cập nhật trạng thái bàn
    @PUT
    @Path("/{id}/status")
    @Transactional
    public Response updateStatus(@PathParam("id") Long id, RestaurantTable body) {
        RestaurantTable table = RestaurantTable.findById(id);
        if (table == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay ban"))
                    .build();
        }
        table.status = body.status;
        return Response.ok(table).build();
    }

    // Cập nhật chi tiết bàn ăn (admin)
    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, RestaurantTable body) {
        RestaurantTable table = RestaurantTable.findById(id);
        if (table == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay ban"))
                    .build();
        }
        if (body.name == null || body.capacity <= 0) {
            return Response.status(400)
                    .entity(Map.of("message", "Ten ban va suc chua khong hop le"))
                    .build();
        }
        table.name = body.name;
        table.capacity = body.capacity;
        table.status = body.status;
        table.description = body.description;
        return Response.ok(table).build();
    }

    // Xóa bàn (admin)
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        RestaurantTable table = RestaurantTable.findById(id);
        if (table == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay ban"))
                    .build();
        }
        try {
            table.delete();
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(400)
                    .entity(Map.of("message", "Khong the xoa ban vi ban dang duoc su dung trong cac lich dat"))
                    .build();
        }
    }
}