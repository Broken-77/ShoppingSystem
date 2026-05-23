package com.wms.shoppingsys.admin;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.auth.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.catalog.ProductDtos;
import com.wms.shoppingsys.common.ApiResponse;
import com.wms.shoppingsys.recommendation.RecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/recommendations")
public class AdminRecommendationController {
    private final RecommendationService recommendationService;
    private final AuthService authService;

    public AdminRecommendationController(RecommendationService recommendationService, AuthService authService) {
        this.recommendationService = recommendationService;
        this.authService = authService;
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<List<ProductDtos.ProductResponse>> userRecommendations(@PathVariable Long userId,
                                                                               HttpServletRequest request) {
        requireAdmin(request);
        return ApiResponse.ok(recommendationService.userRecommendations(userId).stream()
                .map(ProductDtos.ProductResponse::from)
                .toList());
    }

    private CurrentUser requireAdmin(HttpServletRequest request) {
        return authService.requireAdmin((CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE));
    }
}
