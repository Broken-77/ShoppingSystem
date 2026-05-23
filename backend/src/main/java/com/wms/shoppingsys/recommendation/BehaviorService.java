package com.wms.shoppingsys.recommendation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BehaviorService implements BehaviorRecorder {
    private final UserBehaviorRepository userBehaviorRepository;

    public BehaviorService(UserBehaviorRepository userBehaviorRepository) {
        this.userBehaviorRepository = userBehaviorRepository;
    }

    @Override
    @Transactional
    public void recordView(Long userId, Long productId) {
        record(userId, productId, BehaviorType.VIEW, 1);
    }

    @Override
    @Transactional
    public void recordCart(Long userId, Long productId) {
        record(userId, productId, BehaviorType.CART, 4);
    }

    @Override
    @Transactional
    public void recordOrder(Long userId, Long productId) {
        record(userId, productId, BehaviorType.ORDER, 8);
    }

    private void record(Long userId, Long productId, BehaviorType behaviorType, int weight) {
        userBehaviorRepository.save(new UserBehavior(userId, productId, behaviorType, weight));
    }
}
