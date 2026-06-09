package com.wms.shoppingsys.entity;

import com.wms.shoppingsys.enums.ProductStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(length = 80)
    private String brand;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProductStatus status;

    @Column(nullable = false)
    private Integer salesCount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Product() {
    }

    public Product(Long categoryId, String name, String description, String brand, BigDecimal price,
                   Integer stock, String imageUrl, ProductStatus status, Integer salesCount) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.status = status;
        this.salesCount = salesCount;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (salesCount == null) {
            salesCount = 0;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void update(Long categoryId, String name, String description, String brand, BigDecimal price,
                       Integer stock, String imageUrl, ProductStatus status, Integer salesCount) {
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.brand = brand;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.status = status;
        this.salesCount = salesCount;
    }

    public void markOnSale() {
        this.status = ProductStatus.ON_SALE;
    }

    public void markOffSale() {
        this.status = ProductStatus.OFF_SALE;
    }

    public void deductStock(int quantity) {
        this.stock = this.stock - quantity;
    }

    public Long getId() {
        return id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getBrand() {
        return brand;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
