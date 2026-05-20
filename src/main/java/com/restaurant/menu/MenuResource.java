package com.restaurant.menu;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Map;

@Path("/api/menu")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MenuResource {

    // Lấy tất cả món (public)
    @GET
    public List<MenuItem> getAll() {
        return MenuItem.findAvailable();
    }

    // Lấy theo danh mục
    @GET
    @Path("/category/{cat}")
    public List<MenuItem> getByCategory(@PathParam("cat") String cat) {
        return MenuItem.findByCategory(cat);
    }

    // Thêm món mới (admin)
    @POST
    @Transactional
    public Response create(MenuItem item) {
        if (item.name == null || item.price == null || item.category == null) {
            return Response.status(400)
                    .entity(Map.of("message", "Vui long dien day du thong tin"))
                    .build();
        }
        item.available = true;
        item.persist();
        return Response.ok(item).status(201).build();
    }

    // Sửa món (admin)
    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, MenuItem body) {
        MenuItem item = MenuItem.findById(id);
        if (item == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay mon an"))
                    .build();
        }
        if (body.name != null) item.name = body.name;
        if (body.price != null) item.price = body.price;
        if (body.description != null) item.description = body.description;
        if (body.category != null) item.category = body.category;
        if (body.icon != null) item.icon = body.icon;
        item.popular = body.popular;
        item.available = body.available;
        return Response.ok(item).build();
    }

    // Xóa món (admin)
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        MenuItem item = MenuItem.findById(id);
        if (item == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay mon an"))
                    .build();
        }
        item.delete();
        return Response.noContent().build();
    }
}