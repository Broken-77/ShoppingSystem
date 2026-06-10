package com.wms.shoppingsys.service;

import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.entity.UserBehavior;
import com.wms.shoppingsys.enums.BehaviorType;
import com.wms.shoppingsys.enums.ProductStatus;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.repository.UserBehaviorRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ItemBasedCollaborativeFilteringEngine implements RecommendationEngine {
    private static final double FULL_CONFIDENCE_COMMON_USERS = 3.0;
    private static final double MIN_SIMILAR_USER_SCORE = 0.05;
    private static final double SIMILAR_USER_WEIGHT = 1.2;

    private final UserBehaviorRepository userBehaviorRepository;
    private final ProductRepository productRepository;

    public ItemBasedCollaborativeFilteringEngine(UserBehaviorRepository userBehaviorRepository,
                                                  ProductRepository productRepository) {
        this.userBehaviorRepository = userBehaviorRepository;
        this.productRepository = productRepository;
    }

    static double dampedBehaviorScore(int baseWeight, long count, double timeDecay) {
        if (count <= 0) return 0;
        return baseWeight * (1.0 + Math.log(count)) * timeDecay;
    }

    private double timeDecay(Instant createdAt) {
        long daysAgo = Duration.between(createdAt, Instant.now()).toDays();
        if (daysAgo < 1) return 1.0;
        if (daysAgo < 3) return 0.9;
        if (daysAgo < 7) return 0.75;
        if (daysAgo < 30) return 0.5;
        return 0.2;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> recommendProductIds(Long userId, int limit) {
        Map<BehaviorKey, BehaviorAggregate> aggregates = aggregateBehaviors(userBehaviorRepository.findAll());
        Map<Long, Map<Long, Double>> productVectors = productVectors(aggregates);
        Map<Long, Double> targetScores = userProductScores(aggregates, userId);
        Map<Long, Double> directInterests = directInterests(aggregates, userId);
        Map<Long, Instant> latestDirectActivities = latestDirectActivities(aggregates, userId);
        Set<Long> purchasedProductIds = purchasedProductIds(aggregates, userId);
        Map<Long, Product> productsById = productRepository.findAll().stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        Map<Long, Double> similarUserScores = similarUserScores(aggregates, userId, productsById);

        return productsById.values().stream()
                .filter(this::available)
                .filter(product -> !purchasedProductIds.contains(product.getId()))
                .map(product -> {
                    double directInterest = directInterests.getOrDefault(product.getId(), 0.0);
                    double score = directInterest
                            + recommendationScore(productVectors, targetScores, productsById, product)
                            + similarUserScores.getOrDefault(product.getId(), 0.0);
                    return new HomeCandidate(product.getId(), score, latestDirectActivities.get(product.getId()));
                })
                .filter(candidate -> candidate.score() > 0)
                .sorted(Comparator.comparing(HomeCandidate::hasDirectInterest).reversed()
                        .thenComparing(HomeCandidate::latestDirectAt,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(Comparator.comparingDouble(HomeCandidate::score).reversed())
                        .thenComparing(HomeCandidate::productId))
                .limit(limit)
                .map(HomeCandidate::productId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> similarProductIds(Long productId, int limit) {
        Product source = productRepository.findById(productId).orElse(null);
        if (source == null) return List.of();

        Map<Long, Map<Long, Double>> productVectors = productVectors(
                aggregateBehaviors(userBehaviorRepository.findAll())
        );
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

    private double recommendationScore(Map<Long, Map<Long, Double>> productVectors,
                                       Map<Long, Double> targetScores,
                                       Map<Long, Product> productsById,
                                       Product candidate) {
        return targetScores.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(candidate.getId()))
                .filter(entry -> sameCategory(productsById.get(entry.getKey()), candidate))
                .mapToDouble(entry -> entry.getValue()
                        * adjustedSimilarity(productVectors, entry.getKey(), candidate.getId()))
                .sum();
    }

    private Map<Long, Double> similarUserScores(Map<BehaviorKey, BehaviorAggregate> aggregates,
                                                Long userId,
                                                Map<Long, Product> productsById) {
        Map<Long, Map<Long, Double>> userVectors = userCategoryVectors(aggregates, productsById);
        Map<Long, Double> targetVector = userVectors.getOrDefault(userId, Map.of());
        if (targetVector.isEmpty()) return Map.of();

        Long bestUserId = null;
        double bestSimilarity = 0;
        for (Map.Entry<Long, Map<Long, Double>> entry : userVectors.entrySet()) {
            if (entry.getKey().equals(userId)) continue;
            double similarity = cosine(targetVector, entry.getValue());
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestUserId = entry.getKey();
            }
        }
        if (bestUserId == null || bestSimilarity <= MIN_SIMILAR_USER_SCORE) return Map.of();

        Set<Long> targetCategories = aggregates.keySet().stream()
                .filter(key -> userId.equals(key.userId()))
                .map(key -> productsById.get(key.productId()))
                .filter(Objects::nonNull)
                .map(Product::getCategoryId)
                .collect(Collectors.toSet());
        double score = bestSimilarity * SIMILAR_USER_WEIGHT;
        Map<Long, Double> scores = new HashMap<>();
        for (BehaviorKey key : aggregates.keySet()) {
            Product product = productsById.get(key.productId());
            if (bestUserId.equals(key.userId())
                    && product != null
                    && targetCategories.contains(product.getCategoryId())) {
                scores.put(key.productId(), score);
            }
        }
        return scores;
    }

    private Map<Long, Map<Long, Double>> userCategoryVectors(
            Map<BehaviorKey, BehaviorAggregate> aggregates,
            Map<Long, Product> productsById) {
        Map<Long, Map<Long, Double>> vectors = new HashMap<>();
        aggregates.forEach((key, aggregate) -> {
            Product product = productsById.get(key.productId());
            if (product != null) {
                vectors.computeIfAbsent(key.userId(), ignored -> new HashMap<>())
                        .merge(product.getCategoryId(), aggregate.score(), Double::sum);
            }
        });
        return vectors;
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

    private boolean sameCategory(Product source, Product candidate) {
        return source != null && Objects.equals(source.getCategoryId(), candidate.getCategoryId());
    }

    private double similarity(Map<Long, Map<Long, Double>> productVectors,
                              Long leftProductId,
                              Long rightProductId) {
        return cosine(
                productVectors.getOrDefault(leftProductId, Map.of()),
                productVectors.getOrDefault(rightProductId, Map.of())
        );
    }

    private double cosine(Map<Long, Double> left, Map<Long, Double> right) {
        if (left.isEmpty() || right.isEmpty()) return 0;
        Set<Long> keys = new HashSet<>(left.keySet());
        keys.addAll(right.keySet());
        double dotProduct = 0;
        double leftNorm = 0;
        double rightNorm = 0;
        for (Long key : keys) {
            double leftValue = left.getOrDefault(key, 0.0);
            double rightValue = right.getOrDefault(key, 0.0);
            dotProduct += leftValue * rightValue;
            leftNorm += leftValue * leftValue;
            rightNorm += rightValue * rightValue;
        }
        if (leftNorm == 0 || rightNorm == 0) return 0;
        return dotProduct / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private Map<BehaviorKey, BehaviorAggregate> aggregateBehaviors(List<UserBehavior> behaviors) {
        Map<BehaviorKey, BehaviorAggregate> aggregates = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            BehaviorKey key = new BehaviorKey(
                    behavior.getUserId(), behavior.getProductId(), behavior.getBehaviorType()
            );
            aggregates.computeIfAbsent(key, ignored -> new BehaviorAggregate()).add(behavior);
        }
        return aggregates;
    }

    private Map<Long, Map<Long, Double>> productVectors(Map<BehaviorKey, BehaviorAggregate> aggregates) {
        Map<Long, Map<Long, Double>> vectors = new HashMap<>();
        aggregates.forEach((key, aggregate) ->
                vectors.computeIfAbsent(key.productId(), ignored -> new HashMap<>())
                        .merge(key.userId(), aggregate.score(), Double::sum)
        );
        return vectors;
    }

    private Map<Long, Double> userProductScores(Map<BehaviorKey, BehaviorAggregate> aggregates, Long userId) {
        Map<Long, Double> scores = new HashMap<>();
        aggregates.forEach((key, aggregate) -> {
            if (userId.equals(key.userId())) {
                scores.merge(key.productId(), aggregate.score(), Double::sum);
            }
        });
        return scores;
    }

    private Map<Long, Double> directInterests(Map<BehaviorKey, BehaviorAggregate> aggregates, Long userId) {
        Map<Long, Double> scores = new HashMap<>();
        aggregates.forEach((key, aggregate) -> {
            if (userId.equals(key.userId()) && key.behaviorType() != BehaviorType.ORDER) {
                scores.merge(key.productId(), aggregate.score(), Double::sum);
            }
        });
        return scores;
    }

    private Map<Long, Instant> latestDirectActivities(Map<BehaviorKey, BehaviorAggregate> aggregates,
                                                      Long userId) {
        Map<Long, Instant> latestActivities = new HashMap<>();
        aggregates.forEach((key, aggregate) -> {
            if (userId.equals(key.userId()) && key.behaviorType() != BehaviorType.ORDER) {
                latestActivities.merge(key.productId(), aggregate.latestAt(),
                        (left, right) -> left.isAfter(right) ? left : right);
            }
        });
        return latestActivities;
    }

    private Set<Long> purchasedProductIds(Map<BehaviorKey, BehaviorAggregate> aggregates, Long userId) {
        Set<Long> purchased = new HashSet<>();
        aggregates.keySet().stream()
                .filter(key -> userId.equals(key.userId()))
                .filter(key -> key.behaviorType() == BehaviorType.ORDER)
                .map(BehaviorKey::productId)
                .forEach(purchased::add);
        return purchased;
    }

    private boolean available(Product product) {
        return product.getStatus() == ProductStatus.ON_SALE && product.getStock() > 0;
    }

    private record BehaviorKey(Long userId, Long productId, BehaviorType behaviorType) {
    }

    private final class BehaviorAggregate {
        private int baseWeight;
        private long count;
        private Instant latestAt;

        private void add(UserBehavior behavior) {
            baseWeight = Math.max(baseWeight, behavior.getWeight());
            count++;
            if (latestAt == null || behavior.getCreatedAt().isAfter(latestAt)) {
                latestAt = behavior.getCreatedAt();
            }
        }

        private double score() {
            return dampedBehaviorScore(baseWeight, count, timeDecay(latestAt));
        }

        private Instant latestAt() {
            return latestAt;
        }
    }

    private record ScoredProduct(Long productId, double score) {
    }

    private record HomeCandidate(Long productId, double score, Instant latestDirectAt) {
        private boolean hasDirectInterest() {
            return latestDirectAt != null;
        }
    }
}
