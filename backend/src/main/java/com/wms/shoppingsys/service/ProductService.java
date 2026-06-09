package com.wms.shoppingsys.service;

import com.wms.shoppingsys.dto.ProductDtos;
import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.enums.ProductStatus;
import com.wms.shoppingsys.repository.ProductRepository;

import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.common.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> listOnSale(Long categoryId, String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (categoryId != null && !normalizedKeyword.isBlank()) {
            return productRepository.findByStatus(ProductStatus.ON_SALE).stream()
                    .filter(product -> categoryId.equals(product.getCategoryId()))
                    .filter(product -> product.getName().toLowerCase().contains(normalizedKeyword.toLowerCase()))
                    .sorted(Comparator.comparing(Product::getId))
                    .toList();
        }
        if (categoryId != null) {
            return productRepository.findByStatusAndCategoryId(ProductStatus.ON_SALE, categoryId);
        }
        if (!normalizedKeyword.isBlank()) {
            return productRepository.findByStatusAndNameContainingIgnoreCase(ProductStatus.ON_SALE, normalizedKeyword);
        }
        return productRepository.findByStatus(ProductStatus.ON_SALE);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "商品不存在"));
    }

    @Transactional(readOnly = true)
    public Product getOnSaleProduct(Long id) {
        Product product = getProduct(id);
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    @Transactional(readOnly = true)
    public List<Product> similarProducts(Long id) {
        Product product = getOnSaleProduct(id);
        return productRepository.findByStatusAndCategoryId(ProductStatus.ON_SALE, product.getCategoryId()).stream()
                .filter(candidate -> !candidate.getId().equals(id))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Product> listAll() {
        return productRepository.findAll();
    }

    @Transactional
    public Product create(ProductDtos.ProductRequest request) {
        return productRepository.save(request.toProduct());
    }

    @Transactional
    public Product update(Long id, ProductDtos.ProductRequest request) {
        Product product = getProduct(id);
        product.update(
                request.categoryId(),
                request.name(),
                request.description(),
                request.brand(),
                request.price(),
                request.stock(),
                request.imageUrl(),
                request.status() == null ? product.getStatus() : request.status(),
                request.salesCount() == null ? product.getSalesCount() : request.salesCount()
        );
        return product;
    }

    @Transactional
    public Product markOnSale(Long id) {
        Product product = getProduct(id);
        product.markOnSale();
        return product;
    }

    @Transactional
    public Product markOffSale(Long id) {
        Product product = getProduct(id);
        product.markOffSale();
        return product;
    }
}
