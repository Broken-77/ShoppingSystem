package com.wms.shoppingsys.service;

import java.util.List;

public interface RecommendationEngine {
    List<Long> recommendProductIds(Long userId, int limit);

    List<Long> similarProductIds(Long productId, int limit);
}
