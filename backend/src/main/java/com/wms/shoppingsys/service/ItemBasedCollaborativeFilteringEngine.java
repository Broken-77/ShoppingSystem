package com.wms.shoppingsys.service;

import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.entity.UserBehavior;
import com.wms.shoppingsys.enums.ProductStatus;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.repository.UserBehaviorRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ItemBasedCollaborativeFilteringEngine implements RecommendationEngine {
    private static final double FULL_CONFIDENCE_COMMON_USERS = 3.0;

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

        // 执行 CF 评分
        Map<Long, Double> scored = new HashMap<>();
        for (Long productId : productVectors.keySet()) {
            if (seenProducts.contains(productId)) continue;
            double score = recommendationScore(productVectors, targetScores, productId);
            Long catId = productRepository.findById(productId)
                    .map(Product::getCategoryId).orElse(null);
            if (catId != null && favoriteCats.contains(catId)) score *= 1.5;
            if (score > 0) scored.put(productId, score);
        }

        // 找出相似用户，加入他们喜欢的产品
        Map<Long, Map<Long, Double>> userVectors = userVectors(behaviors);
        Map.Entry<Long, Double> bestSimilar = null;
        for (Map.Entry<Long, Map<Long, Double>> entry : userVectors.entrySet()) {
            if (entry.getKey().equals(userId)) continue;
            double sim = cosine(userVectors.getOrDefault(userId, Map.of()), entry.getValue());
            if (bestSimilar == null || sim > bestSimilar.getValue()) {
                bestSimilar = Map.entry(entry.getKey(), sim);
            }
        }
        if (bestSimilar != null && bestSimilar.getValue() > 0.05) {
            Long otherUserId = bestSimilar.getKey();
            double simWeight = bestSimilar.getValue() * 1.2; // 相似用户权重
            for (Product p : productRepository.findAll()) {
                if (seenProducts.contains(p.getId()) || scored.containsKey(p.getId())) continue;
                if (behaviors.stream().anyMatch(b -> b.getUserId().equals(otherUserId) && b.getProductId().equals(p.getId()))) {
                    scored.merge(p.getId(), simWeight, Double::sum);
                }
            }
        }

        return scored.entrySet().stream()
                .map(e -> new ScoredProduct(e.getKey(), e.getValue()))
                .filter(sc -> sc.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed()
                        .thenComparing(ScoredProduct::productId))
                .limit(limit)
                .map(ScoredProduct::productId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> similarProductIds(Long productId, int limit) {
        Product source = productRepository.findById(productId).orElse(null);
        if (source == null) return List.of();

        Map<Long, Map<Long, Double>> productVectors = productVectors(userBehaviorRepository.findAll());
        return productRepository.findByStatusAndCategoryId(ProductStatus.ON_SALE, source.getCategoryId()).stream()
                .filter(candidate -> !candidate.getId().equals(productId))
                .filter(candidate -> candidate.getStock() > 0)
                .map(candidate -> new ScoredProduct(
                        candidate.getId(), adjustedSimilarity(productVectors, productId, candidate.getId())
                ))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingDouble(ScoredProduct::score).reversed()
                        .thenComparing(ScoredProduct::productId))
                .limit(limit)
                .map(ScoredProduct::productId)
                .toList();
    }

    private double adjustedSimilarity(Map<Long, Map<Long, Double>> productVectors,
                                      Long leftProductId,
                                      Long rightProductId) {
        Map<Long, Double> left = productVectors.getOrDefault(leftProductId, Map.of());
        Map<Long, Double> right = productVectors.getOrDefault(rightProductId, Map.of());
        long commonUserCount = left.keySet().stream().filter(right::containsKey).count();
        double confidence = Math.min(commonUserCount / FULL_CONFIDENCE_COMMON_USERS, 1.0);
        return similarity(productVectors, leftProductId, rightProductId) * confidence;
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

    // 构建用户向量：每个用户→{品类→权重}
    private Map<Long, Map<Long, Double>> userVectors(List<UserBehavior> behaviors) {
        Map<Long, Map<Long, Double>> vectors = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            productRepository.findById(behavior.getProductId()).ifPresent(p -> {
                double weight = behavior.getWeight().doubleValue() * timeDecay(behavior);
                vectors.computeIfAbsent(behavior.getUserId(), ignored -> new HashMap<>())
                        .merge(p.getCategoryId(), weight, Double::sum);
            });
        }
        return vectors;
    }

    // 两个权重向量的余弦相似度
    private double cosine(Map<Long, Double> a, Map<Long, Double> b) {
        Set<Long> keys = new HashSet<>(a.keySet()); keys.addAll(b.keySet());
        double dot = 0, normA = 0, normB = 0;
        for (Long k : keys) {
            double va = a.getOrDefault(k, 0.0), vb = b.getOrDefault(k, 0.0);
            dot += va * vb; normA += va * va; normB += vb * vb;
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record ScoredProduct(Long productId, double score) {
    }
}
