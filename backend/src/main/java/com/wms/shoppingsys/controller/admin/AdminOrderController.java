package com.wms.shoppingsys.controller.admin;

import com.wms.shoppingsys.auth.AuthInterceptor;
import com.wms.shoppingsys.service.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.common.ApiResponse;
import com.wms.shoppingsys.entity.Order;
import com.wms.shoppingsys.dto.OrderDtos;
import com.wms.shoppingsys.entity.User;
import com.wms.shoppingsys.repository.UserRepository;
import com.wms.shoppingsys.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;
    private final AuthService authService;
    private final UserRepository userRepository;

    public AdminOrderController(OrderService orderService, AuthService authService, UserRepository userRepository) {
        this.orderService = orderService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ApiResponse<List<OrderDtos.OrderResponse>> list(HttpServletRequest request) {
        requireAdmin(request);
        List<Order> orderRows = orderService.listAll();
        Map<Long, String> usernames = usernamesById(orderRows);
        List<OrderDtos.OrderResponse> orders = orderRows.stream()
                .map(order -> OrderDtos.OrderResponse.from(order, orderService.getItems(order.getId()),
                        usernames.get(order.getUserId())))
                .toList();
        return ApiResponse.ok(orders);
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderDtos.OrderResponse> detail(@PathVariable Long id, HttpServletRequest request) {
        requireAdmin(request);
        Order order = orderService.getOrder(id);
        return ApiResponse.ok(OrderDtos.OrderResponse.from(order, orderService.getItems(order.getId()),
                username(order.getUserId())));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<OrderDtos.OrderResponse> updateStatus(@PathVariable Long id,
                                                             @Valid @RequestBody OrderDtos.UpdateOrderStatusRequest body,
                                                             HttpServletRequest request) {
        requireAdmin(request);
        Order order = orderService.updateStatus(id, body.status());
        return ApiResponse.ok(OrderDtos.OrderResponse.from(order, orderService.getItems(order.getId()),
                username(order.getUserId())));
    }

    private CurrentUser requireAdmin(HttpServletRequest request) {
        return authService.requireAdmin((CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE));
    }

    private Map<Long, String> usernamesById(List<Order> orders) {
        List<Long> userIds = orders.stream()
                .map(Order::getUserId)
                .distinct()
                .toList();
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    private String username(Long userId) {
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("用户 #" + userId);
    }
}
