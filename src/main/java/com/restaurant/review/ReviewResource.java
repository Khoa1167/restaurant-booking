package com.restaurant.review;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Map;

@Path("/api/reviews")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReviewResource {

    // Lấy tất cả review (public)
    @GET
    public List<Review> getAll() {
        return Review.listAll();
    }

    // Tạo review mới
    @POST
    @Transactional
    public Response create(ReviewRequest req) {
        if (req.rating < 1 || req.rating > 5) {
            return Response.status(400)
                    .entity(Map.of("message", "Rating phai tu 1 den 5"))
                    .build();
        }
        if (req.comment == null || req.comment.trim().isEmpty()) {
            return Response.status(400)
                    .entity(Map.of("message", "Vui long viet nhan xet"))
                    .build();
        }

        Review review = new Review();
        review.rating   = req.rating;
        review.comment  = req.comment;
        review.userName = req.userName != null ? req.userName : "Khach hang";
        review.persist();

        return Response.ok(review).status(201).build();
    }

    // Xóa review (admin)
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Review review = Review.findById(id);
        if (review == null) {
            return Response.status(404)
                    .entity(Map.of("message", "Khong tim thay review"))
                    .build();
        }
        review.delete();
        return Response.noContent().build();
    }

    // ── DTO ──────────────────────────────────────
    public static class ReviewRequest {
        public Integer rating;
        public String comment;
        public String userName;
        public Long reservationId;
    }
}