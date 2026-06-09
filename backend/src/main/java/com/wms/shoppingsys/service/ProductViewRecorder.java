package com.wms.shoppingsys.service;

public interface ProductViewRecorder {
    void recordView(Long userId, Long productId);
}
