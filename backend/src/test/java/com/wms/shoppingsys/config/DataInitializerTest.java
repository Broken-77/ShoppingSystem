package com.wms.shoppingsys.config;

import com.wms.shoppingsys.entity.Category;
import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.entity.UserBehavior;
import com.wms.shoppingsys.enums.BehaviorType;
import com.wms.shoppingsys.enums.ProductStatus;
import com.wms.shoppingsys.repository.CategoryRepository;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.repository.UserBehaviorRepository;
import com.wms.shoppingsys.repository.UserRepository;
import com.wms.shoppingsys.service.BehaviorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DataInitializerTest {
    @Autowired UserRepository userRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired ProductRepository productRepository;
    @Autowired BehaviorService behaviorService;
    @Autowired UserBehaviorRepository userBehaviorRepository;

    @BeforeEach
    void setUp() {
        userBehaviorRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void fillsMissingSeedCategoriesWhenCategoryTableAlreadyHasRows() throws Exception {
        categoryRepository.save(new Category("旧分类", null, true, 99));
        DataInitializer initializer = new DataInitializer(userRepository, categoryRepository,
                productRepository, behaviorService, userBehaviorRepository);

        initializer.run();

        assertThat(categoryRepository.findAll())
                .extracting(Category::getName)
                .contains("旧分类", "手机数码", "电脑办公", "家居生活", "运动户外", "美妆个护", "食品饮料");
    }

    @Test
    void fillsMissingSeedProductsWhenProductTableAlreadyHasRows() throws Exception {
        categoryRepository.save(new Category("旧分类", null, true, 99));
        Category phone = categoryRepository.save(new Category("手机数码", null, true, 1));
        categoryRepository.saveAll(List.of(
                new Category("电脑办公", null, true, 2),
                new Category("家居生活", null, true, 3),
                new Category("运动户外", null, true, 4),
                new Category("美妆个护", null, true, 5),
                new Category("食品饮料", null, true, 6)
        ));
        Product existing = productRepository.save(new Product(phone.getId(), "旧商品", "旧商品", "旧品牌",
                new BigDecimal("10.00"), 1, "", ProductStatus.OFF_SALE, 7));
        DataInitializer initializer = new DataInitializer(userRepository, categoryRepository,
                productRepository, behaviorService, userBehaviorRepository);

        initializer.run();

        assertThat(productRepository.findAll())
                .extracting(Product::getName)
                .contains("旧商品", "iPhone 15 Pro Max", "茅台 飞天53度 500ml");
        assertThat(productRepository.count()).isGreaterThan(100);
        Product preserved = productRepository.findById(existing.getId()).orElseThrow();
        assertThat(preserved.getCategoryId()).isEqualTo(phone.getId());
        assertThat(preserved.getBrand()).isEqualTo("旧品牌");
        assertThat(preserved.getPrice()).isEqualByComparingTo("10.00");
        assertThat(preserved.getStock()).isEqualTo(1);
        assertThat(preserved.getStatus()).isEqualTo(ProductStatus.OFF_SALE);
        assertThat(preserved.getSalesCount()).isEqualTo(7);
    }

    @Test
    void updatesOnlyImageForExistingSeedProduct() throws Exception {
        Category phone = categoryRepository.save(new Category("手机数码", null, true, 1));
        categoryRepository.saveAll(List.of(
                new Category("电脑办公", null, true, 2),
                new Category("家居生活", null, true, 3),
                new Category("运动户外", null, true, 4),
                new Category("美妆个护", null, true, 5),
                new Category("食品饮料", null, true, 6)
        ));
        Product existing = productRepository.save(new Product(phone.getId(), "iPhone 15 Pro Max",
                "保留的描述", "保留的品牌", new BigDecimal("123.45"), 7,
                "https://example.com/old-image.jpg", ProductStatus.OFF_SALE, 88));
        userBehaviorRepository.save(new UserBehavior(999L, existing.getId(), BehaviorType.VIEW, 1));
        DataInitializer initializer = new DataInitializer(userRepository, categoryRepository,
                productRepository, behaviorService, userBehaviorRepository);

        initializer.run();

        Product updated = productRepository.findById(existing.getId()).orElseThrow();
        assertThat(updated.getImageUrl()).isEqualTo(
                "https://images.pexels.com/photos/3945672/pexels-photo-3945672.jpeg?w=400&h=300&fit=crop");
        assertThat(updated.getCategoryId()).isEqualTo(phone.getId());
        assertThat(updated.getName()).isEqualTo("iPhone 15 Pro Max");
        assertThat(updated.getDescription()).isEqualTo("保留的描述");
        assertThat(updated.getBrand()).isEqualTo("保留的品牌");
        assertThat(updated.getPrice()).isEqualByComparingTo("123.45");
        assertThat(updated.getStock()).isEqualTo(7);
        assertThat(updated.getStatus()).isEqualTo(ProductStatus.OFF_SALE);
        assertThat(updated.getSalesCount()).isEqualTo(88);
        assertThat(updated.getCreatedAt()).isEqualTo(existing.getCreatedAt());
        assertThat(updated.getUpdatedAt()).isEqualTo(existing.getUpdatedAt());
    }
}
