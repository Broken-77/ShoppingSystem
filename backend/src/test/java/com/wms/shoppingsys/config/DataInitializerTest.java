package com.wms.shoppingsys.config;

import com.wms.shoppingsys.entity.Category;
import com.wms.shoppingsys.entity.Product;
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
        Category phone = categoryRepository.save(new Category("手机数码", null, true, 1));
        categoryRepository.saveAll(List.of(
                new Category("电脑办公", null, true, 2),
                new Category("家居生活", null, true, 3),
                new Category("运动户外", null, true, 4),
                new Category("美妆个护", null, true, 5),
                new Category("食品饮料", null, true, 6)
        ));
        productRepository.save(new Product(phone.getId(), "旧商品", "旧商品", "旧品牌",
                new BigDecimal("10.00"), 1, "", ProductStatus.ON_SALE, 0));
        DataInitializer initializer = new DataInitializer(userRepository, categoryRepository,
                productRepository, behaviorService, userBehaviorRepository);

        initializer.run();

        assertThat(productRepository.findAll())
                .extracting(Product::getName)
                .contains("旧商品", "iPhone 15 Pro Max", "茅台 飞天53度 500ml");
        assertThat(productRepository.count()).isGreaterThan(100);
    }
}
