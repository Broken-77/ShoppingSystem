package com.wms.shoppingsys.service;

import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.entity.UserBehavior;
import com.wms.shoppingsys.enums.BehaviorType;
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
class ItemBasedCollaborativeFilteringEngineTest {
    @Autowired UserBehaviorRepository userBehaviorRepository;
    @Autowired ProductRepository productRepository;
    @Autowired RecommendationEngine recommendationEngine;

    @BeforeEach
    void setUp() {
        userBehaviorRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void recommendsProductWithHigherItemSimilarityFirst() {
        Product source = saveProduct(1L, "source", 10);
        Product closer = saveProduct(1L, "closer", 20);
        Product farther = saveProduct(1L, "farther", 30);

        userBehaviorRepository.save(new UserBehavior(1L, source.getId(), BehaviorType.ORDER, 8));
        userBehaviorRepository.save(new UserBehavior(1L, closer.getId(), BehaviorType.CART, 4));
        userBehaviorRepository.save(new UserBehavior(2L, source.getId(), BehaviorType.ORDER, 8));
        userBehaviorRepository.save(new UserBehavior(2L, closer.getId(), BehaviorType.VIEW, 1));
        userBehaviorRepository.save(new UserBehavior(3L, source.getId(), BehaviorType.VIEW, 1));
        userBehaviorRepository.save(new UserBehavior(3L, farther.getId(), BehaviorType.ORDER, 8));
        userBehaviorRepository.save(new UserBehavior(99L, source.getId(), BehaviorType.VIEW, 1));

        List<Long> recommendations = recommendationEngine.recommendProductIds(99L, 10);
        List<Long> similarProducts = recommendationEngine.similarProductIds(source.getId(), 10);

        assertThat(recommendations).contains(closer.getId(), farther.getId());
        assertThat(recommendations.indexOf(closer.getId())).isLessThan(recommendations.indexOf(farther.getId()));
        assertThat(similarProducts).contains(closer.getId(), farther.getId());
        assertThat(similarProducts.indexOf(closer.getId())).isLessThan(similarProducts.indexOf(farther.getId()));
    }

    @Test
    void similarProductsExcludeCrossCategoryCandidates() {
        Product source = saveProduct(1L, "source", 10);
        Product sameCategory = saveProduct(1L, "same-category", 20);
        Product crossCategory = saveProduct(2L, "cross-category", 30);

        for (long userId = 1; userId <= 3; userId++) {
            userBehaviorRepository.save(new UserBehavior(userId, source.getId(), BehaviorType.ORDER, 8));
            userBehaviorRepository.save(new UserBehavior(userId, crossCategory.getId(), BehaviorType.ORDER, 8));
        }
        userBehaviorRepository.save(new UserBehavior(1L, sameCategory.getId(), BehaviorType.VIEW, 1));

        List<Long> similarProducts = recommendationEngine.similarProductIds(source.getId(), 10);

        assertThat(similarProducts).contains(sameCategory.getId());
        assertThat(similarProducts).doesNotContain(crossCategory.getId());
    }

    @Test
    void commonUserConfidenceReducesSingleUserCoincidence() {
        Product source = saveProduct(1L, "source", 10);
        Product sparseMatch = saveProduct(1L, "sparse-match", 20);
        Product supportedMatch = saveProduct(1L, "supported-match", 30);

        userBehaviorRepository.save(new UserBehavior(1L, source.getId(), BehaviorType.ORDER, 8));
        userBehaviorRepository.save(new UserBehavior(2L, source.getId(), BehaviorType.VIEW, 1));
        userBehaviorRepository.save(new UserBehavior(3L, source.getId(), BehaviorType.VIEW, 1));
        userBehaviorRepository.save(new UserBehavior(1L, sparseMatch.getId(), BehaviorType.ORDER, 8));
        userBehaviorRepository.save(new UserBehavior(1L, supportedMatch.getId(), BehaviorType.VIEW, 1));
        userBehaviorRepository.save(new UserBehavior(2L, supportedMatch.getId(), BehaviorType.VIEW, 1));
        userBehaviorRepository.save(new UserBehavior(3L, supportedMatch.getId(), BehaviorType.VIEW, 1));

        List<Long> similarProducts = recommendationEngine.similarProductIds(source.getId(), 10);

        assertThat(similarProducts.indexOf(supportedMatch.getId()))
                .isLessThan(similarProducts.indexOf(sparseMatch.getId()));
    }

    private Product saveProduct(Long categoryId, String name, int salesCount) {
        return productRepository.save(new Product(
                categoryId, name, name, "brand", BigDecimal.TEN, 10,
                "https://example.com/" + name, ProductStatus.ON_SALE, salesCount
        ));
    }
}
