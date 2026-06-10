package com.wms.shoppingsys.service;

import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.enums.ProductStatus;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.repository.UserBehaviorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RecommendationServiceTest {
    @Autowired ProductRepository productRepository;
    @Autowired UserBehaviorRepository userBehaviorRepository;
    @Autowired RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        userBehaviorRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void similarProductsFallbackUsesSameCategorySalesOnly() {
        Product source = saveProduct(1L, "source", 10);
        Product lowerSales = saveProduct(1L, "lower-sales", 20);
        Product higherSales = saveProduct(1L, "higher-sales", 100);
        Product crossCategory = saveProduct(2L, "cross-category", 1000);

        List<Product> similarProducts = recommendationService.similarProducts(source.getId());

        assertThat(similarProducts)
                .extracting(Product::getId)
                .containsExactly(higherSales.getId(), lowerSales.getId())
                .doesNotContain(crossCategory.getId());
    }

    private Product saveProduct(Long categoryId, String name, int salesCount) {
        return productRepository.save(new Product(
                categoryId, name, name, "shared-brand", BigDecimal.TEN, 10,
                "https://example.com/" + name, ProductStatus.ON_SALE, salesCount
        ));
    }
}
