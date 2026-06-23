package com.restaurant.auth;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.time.Duration;
import java.util.Map;
import java.util.Set;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest req) {
        if (req.name == null || req.email == null || req.password == null) {
            return Response.status(400)
                    .entity(Map.of("message", "Vui long dien day du thong tin"))
                    .build();
        }
        if (User.findByEmail(req.email) != null) {
            return Response.status(400)
                    .entity(Map.of("message", "Email da duoc su dung"))
                    .build();
        }

        User user = new User();
        user.name     = req.name;
        user.email    = req.email;
        user.phone    = req.phone;
        user.password = BcryptUtil.bcryptHash(req.password);
        user.role     = "USER";
        user.persist();

        return Response.ok(Map.of("message", "Dang ky thanh cong")).build();
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest req) {
        if (req.email == null || req.password == null) {
            return Response.status(400)
                    .entity(Map.of("message", "Vui long dien email va mat khau"))
                    .build();
        }

        User user = User.findByEmail(req.email);
        if (user == null || !BcryptUtil.matches(req.password, user.password)) {
            return Response.status(401)
                    .entity(Map.of("message", "Email hoac mat khau khong dung"))
                    .build();
        }

        String token = Jwt.issuer("restaurant-app")
                .subject(String.valueOf(user.id))
                .groups(Set.of(user.role))
                .claim("email", user.email)
                .claim("name",  user.name)
                .claim("role",  user.role)
                .expiresIn(Duration.ofHours(24))
                .sign();

        return Response.ok(Map.of(
                "token", token,
                "user", Map.of(
                        "id",    user.id,
                        "name",  user.name,
                        "email", user.email,
                        "role",  user.role
                )
        )).build();
    }

    @GET
    @Path("/me")
    public Response me() {
        try {
            String subject = jwt.getSubject();
            if (subject != null) {
                User user = User.findById(Long.parseLong(subject));
                if (user != null) {
                    return Response.ok(Map.of(
                            "id",    user.id,
                            "name",  user.name,
                            "email", user.email,
                            "role",  user.role
                    )).build();
                }
            }
        } catch (Exception ignored) {}
        return Response.status(401)
                .entity(Map.of("message", "Chua dang nhap"))
                .build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of("status", "ok")).build();
    }

    public static class RegisterRequest {
        public String name;
        public String email;
        public String phone;
        public String password;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }
}