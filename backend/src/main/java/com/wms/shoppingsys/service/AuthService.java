package com.wms.shoppingsys.service;

import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.auth.TokenStore;
import com.wms.shoppingsys.dto.LoginRequest;
import com.wms.shoppingsys.dto.LoginResponse;
import com.wms.shoppingsys.dto.RegisterRequest;

import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.common.ErrorCode;
import com.wms.shoppingsys.entity.User;
import com.wms.shoppingsys.repository.UserRepository;
import com.wms.shoppingsys.enums.UserRole;
import com.wms.shoppingsys.enums.UserStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenStore tokenStore;

    public AuthService(UserRepository userRepository, TokenStore tokenStore) {
        this.userRepository = userRepository;
        this.tokenStore = tokenStore;
    }

    @Transactional
    public void register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }

        User user = new User(username, hashPassword(request.password()), UserRole.USER, UserStatus.ACTIVE);
        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户名已存在");
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        User user = userRepository.findByUsername(username)
                .filter(found -> found.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHENTICATED, "用户名或密码错误"));

        if (!user.getPasswordHash().equals(hashPassword(request.password()))) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, "用户名或密码错误");
        }

        CurrentUser currentUser = new CurrentUser(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(tokenStore.create(currentUser), user.getUsername(), user.getRole());
    }

    public Optional<CurrentUser> currentUser(String token) {
        return tokenStore.get(token);
    }

    public CurrentUser requireUser(CurrentUser currentUser) {
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHENTICATED, "请先登录");
        }
        return currentUser;
    }

    public CurrentUser requireAdmin(CurrentUser currentUser) {
        CurrentUser user = requireUser(currentUser);
        if (user.role() != UserRole.ADMIN) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "需要管理员权限");
        }
        return user;
    }

    private String normalizeUsername(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
