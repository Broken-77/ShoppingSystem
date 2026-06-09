package com.wms.shoppingsys.auth;

import com.wms.shoppingsys.enums.UserRole;

public record CurrentUser(Long id, String username, UserRole role) {
}
