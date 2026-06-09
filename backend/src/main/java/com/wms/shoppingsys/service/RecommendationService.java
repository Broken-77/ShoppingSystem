package com.wms.shoppingsys.service;

import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.entity.UserBehavior;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.repository.UserBehaviorRepository;
import com.wms.shoppingsys.enums.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    private static final int DEFAULT_LIMIT = 12;

    private final RecommendationEngine recommendationEngine;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final UserBehaviorRepository behaviorRepository;

    public RecommendationService(RecommendationEngine recommendationEngine, ProductRepository productRepository,
                                 ProductService productService, UserBehaviorRepository behaviorRepository) {
        this.recommendationEngine = recommendationEngine;
        this.productRepository = productRepository;
        this.productService = productService;
        this.behaviorRepository = behaviorRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> homeRecommendations(Long userId) {
        List<Product> cfResults = toProducts(recommendationEngine.recommendProductIds(userId, DEFAULT_LIMIT));
        List<Product> categoryHot = categoryHotProducts(userId);
        return mergeProducts(cfResults, categoryHot, newProducts());
    }

    @Transactional(readOnly = true)
    public List<Product> cartRecommendations(Long userId) {
        return homeRecommendations(userId);
    }

    @Transactional(readOnly = true)
    public List<Product> userRecommendations(Long userId) {
        return homeRecommendations(userId);
    }

    @Transactional(readOnly = true)
    public List<Product> similarProducts(Long productId) {
        Product product = productService.getOnSaleProduct(productId);
        List<Product> recommended = toProducts(recommendationEngine.similarProductIds(productId, DEFAULT_LIMIT));
        List<Product> sameCategory = productRepository.findByStatusAndCategoryId(ProductStatus.ON_SALE, product.getCategoryId())
                .stream()
                .filter(candidate -> !candidate.getId().equals(productId))
                .toList();
        List<Product> sameBrand = productRepository.findByStatus(ProductStatus.ON_SALE).stream()
                .filter(candidate -> !candidate.getId().equals(productId))
                .filter(candidate -> product.getBrand() != null && product.getBrand().equals(candidate.getBrand()))
                .toList();
        return mergeProducts(recommended, sameCategory, sameBrand, hotProducts(), newProducts());
    }

    // ── 根据用户行为数据，返回用户最感兴趣品类的热门商品 ────────────
    private List<Product> categoryHotProducts(Long userId) {
        // 找到用户互动最多的品类
        List<UserBehavior> behaviors = behaviorRepository.findByUserId(userId);
        if (behaviors.isEmpty()) return List.of();

        Map<Long, Long> catActivity = new HashMap<>();
        for (UserBehavior b : behaviors) {
            productRepository.findById(b.getProductId()).ifPresent(p -> {
                catActivity.merge(p.getCategoryId(), (long) b.getWeight(), Long::sum);
            });
        }
        if (catActivity.isEmpty()) return List.of();

        // 取权重最高的品类，降序排列
        List<Long> topCats = catActivity.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        // 收集这些品类的热门商品
        List<Product> results = new ArrayList<>();
        for (Long catId : topCats) {
            productRepository.findTop12ByStatusAndCategoryIdOrderBySalesCountDesc(ProductStatus.ON_SALE, catId)
                    .stream()
                    .filter(this::available)
                    .forEach(results::add);
        }
        return results;
    }

    private List<Product> toProducts(List<Long> productIds) {
        return productIds.stream()
                .map(productRepository::findById)
                .flatMap(Optional::stream)
                .filter(this::available)
                .toList();
    }

    @SafeVarargs
    private final List<Product> mergeProducts(List<Product>... productLists) {
        Map<Long, Product> merged = new LinkedHashMap<>();
        for (List<Product> productList : productLists) {
            for (Product product : productList) {
                if (available(product)) {
                    merged.putIfAbsent(product.getId(), product);
                }
                if (merged.size() >= DEFAULT_LIMIT) {
                    return new ArrayList<>(merged.values());
                }
            }
        }
        return new ArrayList<>(merged.values());
    }

    private List<Product> hotProducts() {
        return productRepository.findTop12ByStatusOrderBySalesCountDesc(ProductStatus.ON_SALE);
    }

    private List<Product> newProducts() {
        return productRepository.findTop12ByStatusOrderByCreatedAtDesc(ProductStatus.ON_SALE);
    }

    private boolean available(Product product) {
        return product.getStatus() == ProductStatus.ON_SALE && product.getStock() > 0;
    }
}
