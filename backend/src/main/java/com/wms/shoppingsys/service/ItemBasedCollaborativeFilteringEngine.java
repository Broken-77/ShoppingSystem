package com.wms.shoppingsys.service;

import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.entity.UserBehavior;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.repository.UserBehaviorRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemBasedCollaborativeFilteringEngine implements RecommendationEngine {
    private final UserBehaviorRepository userBehaviorRepository;
    private final ProductRepository productRepository;

    public ItemBasedCollaborativeFilteringEngine(UserBehaviorRepository userBehaviorRepository,
                                                  ProductRepository productRepository) {
        this.userBehaviorRepository = userBehaviorRepository;
        this.productRepository = productRepository;
    }

    // 时间衰减因子：越近的行为权重越高
    private double timeDecay(UserBehavior behavior) {
        long daysAgo = Duration.between(behavior.getCreatedAt(), java.time.Instant.now()).toDays();
        if (daysAgo < 1) return 1.0;
        if (daysAgo < 3) return 0.9;
        if (daysAgo < 7) return 0.75;
        if (daysAgo < 30) return 0.5;
        return 0.2;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> recommendProductIds(Long userId, int limit) {
        List<UserBehavior> behaviors = userBehaviorRepository.findAll();
        Map<Long, Map<Long, Double>> productVectors = productVectors(behaviors);
        Map<Long, Double> targetScores = userProductScores(behaviors, userId);

        // 用户购买过的品类 → 同类产品加权
        Set<Long> favoriteCats = behaviors.stream()
                .filter(b -> userId.equals(b.getUserId()) && b.getWeight() >= 8)
                .map(b -> productRepository.findById(b.getProductId())
                        .map(Product::getCategoryId).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> seenProducts = targetScores.keySet();

        return productVectors.keySet().stream()
                .filter(productId -> !seenProducts.contains(productId))
                .map(productId -> {
                    double score = recommendationScore(productVectors, targetScores, productId);
                    // 品类偏好加成
                    Long catId = productRepository.findById(productId)
                            .map(Product::getCategoryId).orElse(null);
                    if (catId != null && favoriteCats.contains(catId)) {
                        score *= 1.5;
                    }
                    return new ScoredProduct(productId, score);
                })
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed()
                        .thenComparing(ScoredProduct::productId))
                .limit(limit)
                .map(ScoredProduct::productId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> similarProductIds(Long productId, int limit) {
        Map<Long, Map<Long, Double>> productVectors = productVectors(userBehaviorRepository.findAll());
        return productVectors.keySet().stream()
                .filter(candidateId -> !candidateId.equals(productId))
                .map(candidateId -> new ScoredProduct(candidateId, similarity(productVectors, productId, candidateId)))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed()
                        .thenComparing(ScoredProduct::productId))
                .limit(limit)
                .map(ScoredProduct::productId)
                .toList();
    }

    private double recommendationScore(Map<Long, Map<Long, Double>> productVectors,
                                       Map<Long, Double> targetScores,
                                       Long candidateProductId) {
        return targetScores.entrySet().stream()
                .mapToDouble(entry -> entry.getValue()
                        * similarity(productVectors, entry.getKey(), candidateProductId))
                .sum();
    }

    private double similarity(Map<Long, Map<Long, Double>> productVectors, Long leftProductId, Long rightProductId) {
        Map<Long, Double> left = productVectors.getOrDefault(leftProductId, Map.of());
        Map<Long, Double> right = productVectors.getOrDefault(rightProductId, Map.of());
        if (left.isEmpty() || right.isEmpty()) return 0;
        double dotProduct = left.entrySet().stream()
                .mapToDouble(entry -> entry.getValue() * right.getOrDefault(entry.getKey(), 0.0)).sum();
        double leftNorm = Math.sqrt(left.values().stream().mapToDouble(v -> v * v).sum());
        double rightNorm = Math.sqrt(right.values().stream().mapToDouble(v -> v * v).sum());
        if (leftNorm == 0 || rightNorm == 0) return 0;
        return dotProduct / (leftNorm * rightNorm);
    }

    // 构建产品向量时加入时间衰减
    private Map<Long, Map<Long, Double>> productVectors(List<UserBehavior> behaviors) {
        Map<Long, Map<Long, Double>> vectors = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            double weight = behavior.getWeight().doubleValue() * timeDecay(behavior);
            vectors.computeIfAbsent(behavior.getProductId(), ignored -> new HashMap<>())
                    .merge(behavior.getUserId(), weight, Double::sum);
        }
        return vectors;
    }

    // 用户评分时加入时间衰减
    private Map<Long, Double> userProductScores(List<UserBehavior> behaviors, Long userId) {
        return behaviors.stream()
                .filter(behavior -> userId.equals(behavior.getUserId()))
                .collect(Collectors.groupingBy(
                        UserBehavior::getProductId,
                        Collectors.summingDouble(behavior ->
                                behavior.getWeight().doubleValue() * timeDecay(behavior))
                ));
    }

    private record ScoredProduct(Long productId, double score) {
    }
}
