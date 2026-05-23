package com.wms.shoppingsys.recommendation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ItemBasedCollaborativeFilteringEngine implements RecommendationEngine {
    private final UserBehaviorRepository userBehaviorRepository;

    public ItemBasedCollaborativeFilteringEngine(UserBehaviorRepository userBehaviorRepository) {
        this.userBehaviorRepository = userBehaviorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> recommendProductIds(Long userId, int limit) {
        List<UserBehavior> behaviors = userBehaviorRepository.findAll();
        Map<Long, Map<Long, Double>> productVectors = productVectors(behaviors);
        Map<Long, Double> targetScores = userProductScores(behaviors, userId);
        Set<Long> seenProducts = targetScores.keySet();

        return productVectors.keySet().stream()
                .filter(productId -> !seenProducts.contains(productId))
                .map(productId -> new ScoredProduct(productId, recommendationScore(productVectors, targetScores, productId)))
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
        if (left.isEmpty() || right.isEmpty()) {
            return 0;
        }

        double dotProduct = left.entrySet().stream()
                .mapToDouble(entry -> entry.getValue() * right.getOrDefault(entry.getKey(), 0.0))
                .sum();
        double leftNorm = Math.sqrt(left.values().stream().mapToDouble(value -> value * value).sum());
        double rightNorm = Math.sqrt(right.values().stream().mapToDouble(value -> value * value).sum());
        if (leftNorm == 0 || rightNorm == 0) {
            return 0;
        }
        return dotProduct / (leftNorm * rightNorm);
    }

    private Map<Long, Map<Long, Double>> productVectors(List<UserBehavior> behaviors) {
        Map<Long, Map<Long, Double>> vectors = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            vectors.computeIfAbsent(behavior.getProductId(), ignored -> new HashMap<>())
                    .merge(behavior.getUserId(), behavior.getWeight().doubleValue(), Double::sum);
        }
        return vectors;
    }

    private Map<Long, Double> userProductScores(List<UserBehavior> behaviors, Long userId) {
        return behaviors.stream()
                .filter(behavior -> userId.equals(behavior.getUserId()))
                .collect(Collectors.groupingBy(
                        UserBehavior::getProductId,
                        Collectors.summingDouble(behavior -> behavior.getWeight().doubleValue())
                ));
    }

    private record ScoredProduct(Long productId, double score) {
    }
}
