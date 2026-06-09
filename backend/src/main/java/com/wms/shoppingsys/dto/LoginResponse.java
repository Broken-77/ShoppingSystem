package com.wms.shoppingsys.dto;

import com.wms.shoppingsys.enums.UserRole;

public record LoginResponse(String token, String username, UserRole role) {
}
