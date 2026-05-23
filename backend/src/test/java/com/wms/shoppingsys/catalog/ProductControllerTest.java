package com.wms.shoppingsys.catalog;

import com.wms.shoppingsys.auth.AuthService;
import com.wms.shoppingsys.auth.LoginRequest;
import com.wms.shoppingsys.auth.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ProductControllerTest {
    MockMvc mvc;

    @Autowired WebApplicationContext context;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired AuthService authService;

    Category phones;
    Category books;
    Product phone;
    Product book;
    Product offSalePhone;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        phones = categoryRepository.save(new Category("Phones", null, true, 1));
        books = categoryRepository.save(new Category("Books", null, true, 2));

        phone = productRepository.save(new Product(
                phones.getId(),
                "Phone Pro",
                "Flagship phone",
                "Acme",
                new BigDecimal("699.00"),
                10,
                "/images/phone.png",
                ProductStatus.ON_SALE,
                7
        ));
        book = productRepository.save(new Product(
                books.getId(),
                "Clean Code",
                "Programming book",
                "Prentice Hall",
                new BigDecimal("39.00"),
                4,
                "/images/book.png",
                ProductStatus.ON_SALE,
                3
        ));
        offSalePhone = productRepository.save(new Product(
                phones.getId(),
                "Phone Old",
                "Retired phone",
                "Acme",
                new BigDecimal("199.00"),
                0,
                "/images/old-phone.png",
                ProductStatus.OFF_SALE,
                1
        ));
    }

    @Test
    void listsOnlyOnSaleProducts() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[?(@.id == " + phone.getId() + ")]").exists())
                .andExpect(jsonPath("$.data[?(@.id == " + book.getId() + ")]").exists())
                .andExpect(jsonPath("$.data[?(@.id == " + offSalePhone.getId() + ")]").doesNotExist());
    }

    @Test
    void filtersProductsByKeyword() throws Exception {
        mvc.perform(get("/api/products").param("keyword", "phone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Phone Pro"));
    }

    @Test
    void filtersProductsByCategory() throws Exception {
        mvc.perform(get("/api/products").param("categoryId", phones.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(phone.getId()));
    }

    @Test
    void returnsProductDetailForLoggedInUserWithoutBreakingViewRecording() throws Exception {
        String username = "viewer-" + System.nanoTime();
        authService.register(new RegisterRequest(username, "pass123"));
        String token = authService.login(new LoginRequest(username, "pass123")).token();

        mvc.perform(get("/api/products/{id}", phone.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(phone.getId()))
                .andExpect(jsonPath("$.data.name").value("Phone Pro"));
    }
}
