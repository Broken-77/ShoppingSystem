package com.wms.shoppingsys.cart;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.auth.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;
    private final AuthService authService;

    public CartController(CartService cartService, AuthService authService) {
        this.cartService = cartService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<CartDtos.CartItemResponse>> list(HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        List<CartDtos.CartItemResponse> items = cartService.list(user.id()).stream()
                .map(CartDtos.CartItemResponse::from)
                .toList();
        return ApiResponse.ok(items);
    }

    @PostMapping("/items")
    public ApiResponse<CartDtos.CartItemResponse> add(@Valid @RequestBody CartDtos.AddCartItemRequest body,
                                                       HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        return ApiResponse.ok(CartDtos.CartItemResponse.from(cartService.add(user.id(), body)));
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartDtos.CartItemResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody CartDtos.UpdateCartItemRequest body,
                                                         HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        return ApiResponse.ok(CartDtos.CartItemResponse.from(cartService.update(user.id(), id, body)));
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        cartService.delete(user.id(), id);
        return ApiResponse.ok(null);
    }

    private CurrentUser requireUser(HttpServletRequest request) {
        return authService.requireUser((CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE));
    }
}
