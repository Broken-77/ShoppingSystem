package com.wms.shoppingsys.controller.admin;

import com.wms.shoppingsys.common.ApiResponse;
import com.wms.shoppingsys.entity.User;
import com.wms.shoppingsys.repository.UserRepository;
import com.wms.shoppingsys.enums.UserStatus;
import com.wms.shoppingsys.service.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.auth.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;
    private final AuthService authService;

    public AdminUserController(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<User>> list(HttpServletRequest request) {
        authService.requireAdmin(currentUser(request));
        return ApiResponse.ok(userRepository.findAll());
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<User> disable(@PathVariable Long id, HttpServletRequest request) {
        authService.requireAdmin(currentUser(request));
        User user = userRepository.findById(id).orElseThrow();
        user.changeStatus(UserStatus.DISABLED);
        return ApiResponse.ok(userRepository.save(user));
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<User> enable(@PathVariable Long id, HttpServletRequest request) {
        authService.requireAdmin(currentUser(request));
        User user = userRepository.findById(id).orElseThrow();
        user.changeStatus(UserStatus.ACTIVE);
        return ApiResponse.ok(userRepository.save(user));
    }

    private CurrentUser currentUser(HttpServletRequest request) {
        return (CurrentUser) request.getAttribute(AuthInterceptor.CURRENT_USER_ATTRIBUTE);
    }
}
