package com.wms.shoppingsys.controller.admin;

import com.wms.shoppingsys.dto.LoginRequest;
import com.wms.shoppingsys.entity.Order;
import com.wms.shoppingsys.entity.OrderItem;
import com.wms.shoppingsys.entity.User;
import com.wms.shoppingsys.enums.UserRole;
import com.wms.shoppingsys.enums.UserStatus;
import com.wms.shoppingsys.repository.OrderItemRepository;
import com.wms.shoppingsys.repository.OrderRepository;
import com.wms.shoppingsys.repository.UserRepository;
import com.wms.shoppingsys.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminOrderControllerTest {
    MockMvc mvc;

    @Autowired WebApplicationContext context;
    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void adminOrderListIncludesBuyerUsername() throws Exception {
        User buyer = userRepository.save(new User("buyer-one", hashPassword("pass123"), UserRole.USER, UserStatus.ACTIVE));
        userRepository.save(new User("admin-one", hashPassword("pass123"), UserRole.ADMIN, UserStatus.ACTIVE));
        Order order = orderRepository.save(new Order("SO-BUYER-001", buyer.getId(), new BigDecimal("129.00")));
        orderItemRepository.save(new OrderItem(order.getId(), 1L, "MagSafe Phone Case",
                new BigDecimal("129.00"), 1, new BigDecimal("129.00")));

        String token = authService.login(new LoginRequest("admin-one", "pass123")).token();

        mvc.perform(get("/api/admin/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].username", hasItem("buyer-one")));
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
