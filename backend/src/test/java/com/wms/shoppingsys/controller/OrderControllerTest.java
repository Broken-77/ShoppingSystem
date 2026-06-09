package com.wms.shoppingsys.controller;

import com.wms.shoppingsys.entity.Order;
import com.wms.shoppingsys.entity.OrderItem;
import com.wms.shoppingsys.repository.OrderItemRepository;
import com.wms.shoppingsys.repository.OrderRepository;

import com.wms.shoppingsys.service.AuthService;
import com.wms.shoppingsys.dto.LoginRequest;
import com.wms.shoppingsys.dto.RegisterRequest;
import com.wms.shoppingsys.entity.CartItem;
import com.wms.shoppingsys.repository.CartItemRepository;
import com.wms.shoppingsys.entity.Category;
import com.wms.shoppingsys.repository.CategoryRepository;
import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.enums.ProductStatus;
import com.wms.shoppingsys.service.BehaviorRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class OrderControllerTest {
    MockMvc mvc;

    @Autowired WebApplicationContext context;
    @Autowired AuthService authService;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderItemRepository orderItemRepository;
    @Autowired RecordingBehaviorRecorder behaviorRecorder;

    Product product;
    String token;
    String otherToken;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
        cartItemRepository.deleteAll();
        behaviorRecorder.clear();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(new Category("Phones", null, true, 1));
        product = productRepository.save(new Product(
                category.getId(),
                "Phone Pro",
                "Flagship phone",
                "Acme",
                new BigDecimal("699.00"),
                5,
                "/images/phone.png",
                ProductStatus.ON_SALE,
                0
        ));
        token = registerAndLogin("order-user-");
        otherToken = registerAndLogin("other-order-user-");
    }

    @Test
    void createsPendingPaymentOrderFromCartAndDeductsStock() throws Exception {
        cartItemRepository.save(new CartItem(currentUserId(token), product.getId(), 2));

        mvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.data.totalAmount").value(1398.00))
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));

        Product reloaded = productRepository.findById(product.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(reloaded.getStock()).isEqualTo(3);
        org.assertj.core.api.Assertions.assertThat(cartItemRepository.findByUserId(currentUserId(token))).isEmpty();
        org.assertj.core.api.Assertions.assertThat(behaviorRecorder.events())
                .contains("ORDER:" + currentUserId(token) + ":" + product.getId());
    }

    @Test
    void payingOrderChangesStatusToPaidAndSetsPaidAt() throws Exception {
        Order order = createOrderForCurrentUser(1);

        mvc.perform(post("/api/orders/{id}/pay", order.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PAID"))
                .andExpect(jsonPath("$.data.paidAt").isNotEmpty());
    }

    @Test
    void payingSameOrderTwiceReturnsInvalidOrderState() throws Exception {
        Order order = createOrderForCurrentUser(1);
        mvc.perform(post("/api/orders/{id}/pay", order.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mvc.perform(post("/api/orders/{id}/pay", order.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("订单状态不允许该操作"));
    }

    @Test
    void listOrdersReturnsOnlyCurrentUsersOrders() throws Exception {
        Order ownOrder = createOrderForCurrentUser(1);
        orderRepository.save(new Order("SO202605230000009999", currentUserId(otherToken), new BigDecimal("99.00")));

        mvc.perform(get("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(ownOrder.getId()));
    }

    private Order createOrderForCurrentUser(int quantity) {
        Long userId = currentUserId(token);
        Order order = orderRepository.save(new Order("SO202605230000000001", userId,
                product.getPrice().multiply(BigDecimal.valueOf(quantity))));
        orderItemRepository.save(new OrderItem(order.getId(), product.getId(), product.getName(), product.getPrice(),
                quantity, product.getPrice().multiply(BigDecimal.valueOf(quantity))));
        return order;
    }

    private Long currentUserId(String currentToken) {
        return authService.currentUser(currentToken).orElseThrow().id();
    }

    private String registerAndLogin(String prefix) {
        String username = prefix + System.nanoTime();
        authService.register(new RegisterRequest(username, "pass123"));
        return authService.login(new LoginRequest(username, "pass123")).token();
    }

    @TestConfiguration
    static class BehaviorRecorderTestConfig {
        @Bean
        @Primary
        RecordingBehaviorRecorder recordingBehaviorRecorder() {
            return new RecordingBehaviorRecorder();
        }
    }

    static class RecordingBehaviorRecorder implements BehaviorRecorder {
        private final List<String> events = new ArrayList<>();

        @Override
        public void recordView(Long userId, Long productId) {
            events.add("VIEW:" + userId + ":" + productId);
        }

        @Override
        public void recordCart(Long userId, Long productId) {
            events.add("CART:" + userId + ":" + productId);
        }

        @Override
        public void recordOrder(Long userId, Long productId) {
            events.add("ORDER:" + userId + ":" + productId);
        }

        List<String> events() {
            return events;
        }

        void clear() {
            events.clear();
        }
    }
}
