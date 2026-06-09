package com.wms.shoppingsys.controller.admin;

import com.wms.shoppingsys.service.AuthService;
import com.wms.shoppingsys.dto.LoginRequest;
import com.wms.shoppingsys.dto.RegisterRequest;
import com.wms.shoppingsys.entity.User;
import com.wms.shoppingsys.repository.UserRepository;
import com.wms.shoppingsys.enums.UserRole;
import com.wms.shoppingsys.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminAuthorizationTest {
    MockMvc mvc;

    @Autowired WebApplicationContext context;
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void userTokenCannotAccessAdminProducts() throws Exception {
        String username = "user-" + System.nanoTime();
        authService.register(new RegisterRequest(username, "pass123"));
        String token = authService.login(new LoginRequest(username, "pass123")).token();

        mvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminTokenCanAccessAdminProducts() throws Exception {
        String username = "admin-" + System.nanoTime();
        userRepository.save(new User(username, hashPassword("pass123"), UserRole.ADMIN, UserStatus.ACTIVE));
        String token = authService.login(new LoginRequest(username, "pass123")).token();

        mvc.perform(get("/api/admin/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
