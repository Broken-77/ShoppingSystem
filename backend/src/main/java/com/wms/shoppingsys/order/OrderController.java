package com.wms.shoppingsys.order;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.auth.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<OrderDtos.OrderResponse>> list(HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        List<OrderDtos.OrderResponse> orders = orderService.listUserOrders(user.id()).stream()
                .map(order -> OrderDtos.OrderResponse.from(order, orderService.getItems(order.getId())))
                .toList();
        return ApiResponse.ok(orders);
    }

    @PostMapping
    public ApiResponse<OrderDtos.OrderResponse> create(HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        Order order = orderService.createOrder(user.id());
        return ApiResponse.ok(OrderDtos.OrderResponse.from(order, orderService.getItems(order.getId())));
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDtos.OrderResponse> detail(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        Order order = orderService.getOwnedOrder(user.id(), id);
        return ApiResponse.ok(OrderDtos.OrderResponse.from(order, orderService.getItems(order.getId())));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<OrderDtos.OrderResponse> pay(@PathVariable Long id, HttpServletRequest request) {
        CurrentUser user = requireUser(request);
        Order order = orderService.pay(user.id(), id);
        return ApiResponse.ok(OrderDtos.OrderResponse.from(order, orderService.getItems(order.getId())));
    }

    private CurrentUser requireUser(HttpServletRequest request) {
        return authService.requireUser((CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE));
    }
}
