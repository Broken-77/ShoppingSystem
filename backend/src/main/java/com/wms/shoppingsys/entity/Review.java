package com.wms.shoppingsys.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private Long productId;
    @Column(nullable = false) private String username;
    @Column(nullable = false) private Integer rating;
    @Column(length = 1000) private String comment;
    @Column(nullable = false, updatable = false) private Instant createdAt;

    protected Review() {}

    public Review(Long userId, Long productId, String username, Integer rating, String comment) {
        this.userId = userId; this.productId = productId;
        this.username = username; this.rating = rating; this.comment = comment;
    }

    @PrePersist void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getProductId() { return productId; }
    public String getUsername() { return username; }
    public Integer getRating() { return rating; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}
