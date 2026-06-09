package com.wms.shoppingsys.config;

import com.wms.shoppingsys.entity.Category;
import com.wms.shoppingsys.repository.CategoryRepository;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.repository.UserBehaviorRepository;
import com.wms.shoppingsys.repository.UserRepository;
import com.wms.shoppingsys.service.BehaviorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
}
