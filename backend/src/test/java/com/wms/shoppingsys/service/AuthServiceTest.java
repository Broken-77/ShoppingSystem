package com.wms.shoppingsys.service;

import com.wms.shoppingsys.auth.TokenStore;
import com.wms.shoppingsys.dto.RegisterRequest;

import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.common.ErrorCode;
import com.wms.shoppingsys.entity.User;
import com.wms.shoppingsys.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthServiceTest {
    @Test
    void registerMapsUniqueConstraintConflictToBadRequest() {
        UserRepository userRepository = duplicateUsernameRepository();
        AuthService authService = new AuthService(userRepository, new TokenStore());

        assertThatThrownBy(() -> authService.register(new RegisterRequest("race", "pass123")))
                .isInstanceOfSatisfying(BusinessException.class, ex ->
                        assertThat(ex.getCode()).isEqualTo(ErrorCode.BAD_REQUEST));
    }

    private UserRepository duplicateUsernameRepository() {
        return (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "existsByUsername" -> false;
                    case "saveAndFlush" -> throwDuplicateUsername();
                    default -> throw new UnsupportedOperationException(method.getName());
                });
    }

    private Object throwDuplicateUsername() {
        throw new DataIntegrityViolationException("duplicate username");
    }
}
