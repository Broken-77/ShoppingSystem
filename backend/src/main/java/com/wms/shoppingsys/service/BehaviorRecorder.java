package com.wms.shoppingsys.service;

public interface BehaviorRecorder {
    void recordView(Long userId, Long productId);

    void recordCart(Long userId, Long productId);

    void recordOrder(Long userId, Long productId);
}
