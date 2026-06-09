package com.wms.shoppingsys.repository;

import com.wms.shoppingsys.entity.UserBehavior;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBehaviorRepository extends JpaRepository<UserBehavior, Long> {
    List<UserBehavior> findByUserId(Long userId);
}
