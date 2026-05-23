package com.wms.shoppingsys.auth;

import com.wms.shoppingsys.user.UserRole;

public record LoginResponse(String token, String username, UserRole role) {
}
