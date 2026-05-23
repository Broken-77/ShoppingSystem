package com.wms.shoppingsys.recommendation;

import com.wms.shoppingsys.catalog.Product;
import com.wms.shoppingsys.catalog.ProductRepository;
import com.wms.shoppingsys.catalog.ProductService;
import com.wms.shoppingsys.catalog.ProductStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationService {
    private static final int DEFAULT_LIMIT = 12;

    private final RecommendationEngine recommendationEngine;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public RecommendationService(RecommendationEngine recommendationEngine, ProductRepository productRepository,
                                 ProductService productService) {
        this.recommendationEngine = recommendationEngine;
        this.productRepository = productRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<Product> homeRecommendations(Long userId) {
        return fillWithFallback(toProducts(recommendationEngine.recommendProductIds(userId, DEFAULT_LIMIT)));
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

    private List<Product> fillWithFallback(List<Product> products) {
        return mergeProducts(products, hotProducts(), newProducts());
    }

    private List<Product> toProducts(List<Long> productIds) {
        return productIds.stream()
                .map(productRepository::findById)
                .flatMap(java.util.Optional::stream)
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
