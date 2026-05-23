package com.wms.shoppingsys.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    private Long parentId;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private Integer sortOrder;

    protected Category() {
    }

    public Category(String name, Long parentId, Boolean enabled, Integer sortOrder) {
        this.name = name;
        this.parentId = parentId;
        this.enabled = enabled;
        this.sortOrder = sortOrder;
    }

    public void update(String name, Long parentId, Boolean enabled, Integer sortOrder) {
        this.name = name;
        this.parentId = parentId;
        this.enabled = enabled;
        this.sortOrder = sortOrder;
    }

    public void disable() {
        this.enabled = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }
}
