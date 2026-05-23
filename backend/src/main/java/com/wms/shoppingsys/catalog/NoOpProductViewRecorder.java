package com.wms.shoppingsys.catalog;

import org.springframework.stereotype.Component;

@Component
class NoOpProductViewRecorder implements ProductViewRecorder {
    @Override
    public void recordView(Long userId, Long productId) {
    }
}
