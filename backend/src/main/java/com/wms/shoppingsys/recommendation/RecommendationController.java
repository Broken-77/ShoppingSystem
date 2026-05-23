package com.wms.shoppingsys.recommendation;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.auth.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.catalog.ProductDtos;
import com.wms.shoppingsys.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;
    private final AuthService authService;

    public RecommendationController(RecommendationService recommendationService, AuthService authService) {
        this.recommendationService = recommendationService;
        this.authService = authService;
    }

    @GetMapping("/home")
    public ApiResponse<List<ProductDtos.ProductResponse>> home(HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        return ApiResponse.ok(recommendationService.homeRecommendations(user.id()).stream()
                .map(ProductDtos.ProductResponse::from)
                .toList());
    }

    @GetMapping("/cart")
    public ApiResponse<List<ProductDtos.ProductResponse>> cart(HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        return ApiResponse.ok(recommendationService.cartRecommendations(user.id()).stream()
                .map(ProductDtos.ProductResponse::from)
                .toList());
    }

    private CurrentUser requireUser(HttpServletRequest request) {
        return authService.requireUser((CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE));
    }
}
