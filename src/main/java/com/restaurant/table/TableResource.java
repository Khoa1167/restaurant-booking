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

    // Lấy bàn trống
    @GET
    @Path("/available")
    public List<RestaurantTable> getAvailable() {
        return RestaurantTable.findAvailable();
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
        table.status = "AVAILABLE";
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
        table.delete();
        return Response.noContent().build();
    }
}