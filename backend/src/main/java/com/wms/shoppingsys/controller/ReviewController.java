package com.wms.shoppingsys.controller;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.common.ApiResponse;
import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.common.ErrorCode;
import com.wms.shoppingsys.entity.Review;
import com.wms.shoppingsys.repository.ReviewRepository;
import com.wms.shoppingsys.controller.ReviewController.ReviewRequest;
import com.wms.shoppingsys.entity.Order;
import com.wms.shoppingsys.entity.OrderItem;
import com.wms.shoppingsys.enums.OrderStatus;
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
        // 只有已支付/已完成的订单才能评价
        boolean bought = orderRepo.findByUserIdOrderByCreatedAtDesc(user.id()).stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.FINISHED)
                .flatMap(o -> orderItemRepo.findByOrderId(o.getId()).stream())
                .anyMatch(item -> item.getProductId().equals(req.productId));
        if (!bought) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "购买过此商品才能评价");
        }
        // 一人只允许发一条带星主评价
        boolean hasMain = reviewRepo.findByProductIdOrderByCreatedAtDesc(req.productId).stream()
                .anyMatch(r -> r.getUserId().equals(user.id()) && r.getRating() > 0);
        if (hasMain) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "您已评价过，可追评补充");
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

    @PostMapping("/{id}/reply")
    public ApiResponse<Review> reply(@PathVariable Long id, @RequestBody ReplyRequest req, HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        Review parent = reviewRepo.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "评价不存在"));
        // 只能追评自己的主评价
        if (!parent.getUserId().equals(user.id())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能在自己的评价下追评");
        }
        boolean bought = orderRepo.findByUserIdOrderByCreatedAtDesc(user.id()).stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.FINISHED)
                .flatMap(o -> orderItemRepo.findByOrderId(o.getId()).stream())
                .anyMatch(item -> item.getProductId().equals(parent.getProductId()));
        if (!bought) throw new BusinessException(ErrorCode.BAD_REQUEST, "购买过此商品才能追评");
        Review r = reviewRepo.save(new Review(user.id(), parent.getProductId(), user.username(), 0, req.comment()));
        return ApiResponse.ok(r);
    }

    public record ReplyRequest(String comment) {}
}
