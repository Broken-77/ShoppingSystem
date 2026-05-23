package com.wms.shoppingsys.catalog;

public interface ProductViewRecorder {
    void recordView(Long userId, Long productId);
}
