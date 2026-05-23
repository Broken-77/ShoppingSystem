package com.wms.shoppingsys.cart;

import com.wms.shoppingsys.auth.AuthService;
import com.wms.shoppingsys.auth.LoginRequest;
import com.wms.shoppingsys.auth.RegisterRequest;
import com.wms.shoppingsys.catalog.Category;
import com.wms.shoppingsys.catalog.CategoryRepository;
import com.wms.shoppingsys.catalog.Product;
import com.wms.shoppingsys.catalog.ProductRepository;
import com.wms.shoppingsys.catalog.ProductStatus;
import com.wms.shoppingsys.recommendation.BehaviorRecorder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CartControllerTest {
    MockMvc mvc;

    @Autowired WebApplicationContext context;
    @Autowired AuthService authService;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired CartItemRepository cartItemRepository;
    @Autowired RecordingBehaviorRecorder behaviorRecorder;

    Product product;
    String token;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
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
        token = registerAndLogin("cart-user-");
    }

    @Test
    void addingOnSaleProductCreatesCartItem() throws Exception {
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"quantity":2}
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.productId").value(product.getId()))
                .andExpect(jsonPath("$.data.quantity").value(2));

        org.assertj.core.api.Assertions.assertThat(behaviorRecorder.events())
                .contains("CART:" + authService.currentUser(token).orElseThrow().id() + ":" + product.getId());
    }

    @Test
    void addingSameProductIncrementsQuantity() throws Exception {
        addItem(2);

        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"quantity":3}
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productId").value(product.getId()))
                .andExpect(jsonPath("$.data.quantity").value(5));
    }

    @Test
    void addingQuantityBeyondStockReturnsStockNotEnough() throws Exception {
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"quantity":6}
                                """.formatted(product.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("库存不足"));
    }

    @Test
    void deletingCartItemRemovesItFromUserCart() throws Exception {
        addItem(1);
        CartItem item = cartItemRepository.findAll().get(0);

        mvc.perform(delete("/api/cart/items/{id}", item.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    private void addItem(int quantity) throws Exception {
        mvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":%d,"quantity":%d}
                                """.formatted(product.getId(), quantity)))
                .andExpect(status().isOk());
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
