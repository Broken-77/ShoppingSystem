package com.wms.shoppingsys.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ItemBasedCollaborativeFilteringEngineTest {
    @Autowired UserBehaviorRepository userBehaviorRepository;
    @Autowired RecommendationEngine recommendationEngine;

    @BeforeEach
    void setUp() {
        userBehaviorRepository.deleteAll();
    }

    @Test
    void recommendsProductWithHigherItemSimilarityFirst() {
        userBehaviorRepository.save(new UserBehavior(1L, 1L, BehaviorType.ORDER, 8));
        userBehaviorRepository.save(new UserBehavior(1L, 2L, BehaviorType.CART, 4));
        userBehaviorRepository.save(new UserBehavior(2L, 1L, BehaviorType.ORDER, 8));
        userBehaviorRepository.save(new UserBehavior(2L, 2L, BehaviorType.VIEW, 1));
        userBehaviorRepository.save(new UserBehavior(3L, 1L, BehaviorType.VIEW, 1));
        userBehaviorRepository.save(new UserBehavior(3L, 3L, BehaviorType.ORDER, 8));
        userBehaviorRepository.save(new UserBehavior(99L, 1L, BehaviorType.VIEW, 1));

        List<Long> recommendations = recommendationEngine.recommendProductIds(99L, 10);
        List<Long> similarProducts = recommendationEngine.similarProductIds(1L, 10);

        assertThat(recommendations).contains(2L, 3L);
        assertThat(recommendations.indexOf(2L)).isLessThan(recommendations.indexOf(3L));
        assertThat(similarProducts).contains(2L, 3L);
        assertThat(similarProducts.indexOf(2L)).isLessThan(similarProducts.indexOf(3L));
    }
}
