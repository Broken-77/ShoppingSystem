package com.wms.shoppingsys.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_behaviors")
public class UserBehavior {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BehaviorType behaviorType;

    @Column(nullable = false)
    private Integer weight;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected UserBehavior() {
    }

    public UserBehavior(Long userId, Long productId, BehaviorType behaviorType, Integer weight) {
        this.userId = userId;
        this.productId = productId;
        this.behaviorType = behaviorType;
        this.weight = weight;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public BehaviorType getBehaviorType() {
        return behaviorType;
    }

    public Integer getWeight() {
        return weight;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
