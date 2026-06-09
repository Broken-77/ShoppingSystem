package com.wms.shoppingsys.repository;

import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.enums.ProductStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);

    List<Product> findByStatusAndCategoryId(ProductStatus status, Long categoryId);

    List<Product> findByStatusAndNameContainingIgnoreCase(ProductStatus status, String keyword);

    List<Product> findTop12ByStatusOrderBySalesCountDesc(ProductStatus status);

    List<Product> findTop12ByStatusAndCategoryIdOrderBySalesCountDesc(ProductStatus status, Long categoryId);

    List<Product> findTop12ByStatusOrderByCreatedAtDesc(ProductStatus status);
}
