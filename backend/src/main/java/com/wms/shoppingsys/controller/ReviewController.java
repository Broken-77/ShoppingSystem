package com.wms.shoppingsys.controller;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.common.ApiResponse;
import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.common.ErrorCode;
import com.wms.shoppingsys.entity.Review;
import com.wms.shoppingsys.repository.ReviewRepository;
import com.wms.shoppingsys.controller.ReviewController.ReviewRequest;
import com.wms.shoppingsys.entity.OrderItem;
import com.wms.shoppingsys.repository.OrderItemRepository;
import com.wms.shoppingsys.repository.OrderRepository;
import com.wms.shoppingsys.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    private final ReviewRepository reviewRepo;
    private final OrderItemRepository orderItemRepo;
    private final OrderRepository orderRepo;
    private final AuthService authService;

    public ReviewController(ReviewRepository reviewRepo, OrderItemRepository orderItemRepo,
                            OrderRepository orderRepo, AuthService authService) {
        this.reviewRepo = reviewRepo; this.orderItemRepo = orderItemRepo;
        this.orderRepo = orderRepo; this.authService = authService;
    }

    @GetMapping("/products/{productId}")
    public ApiResponse<List<Review>> list(@PathVariable Long productId) {
        return ApiResponse.ok(reviewRepo.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @PostMapping
    public ApiResponse<Review> create(@RequestBody ReviewRequest req, HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        // 只有购买过的人才能评价
        boolean bought = orderRepo.findByUserIdOrderByCreatedAtDesc(user.id()).stream()
                .flatMap(o -> orderItemRepo.findByOrderId(o.getId()).stream())
                .anyMatch(item -> item.getProductId().equals(req.productId));
        if (!bought) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "购买过此商品才能评价");
        }
        if (reviewRepo.existsByUserIdAndProductId(user.id(), req.productId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您已经评价过此商品");
        }
        if (req.rating < 1 || req.rating > 5) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "评分需在1-5之间");
        }
        Review r = reviewRepo.save(new Review(user.id(), req.productId, user.username(), req.rating, req.comment));
        return ApiResponse.ok(r);
    }

    private CurrentUser requireUser(HttpServletRequest request) {
        return authService.requireUser((CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE));
    }

    public record ReviewRequest(Long productId, int rating, String comment) {}
}
