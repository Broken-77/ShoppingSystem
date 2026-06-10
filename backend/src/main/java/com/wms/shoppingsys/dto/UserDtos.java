package com.wms.shoppingsys.dto;

import java.util.List;
import java.util.Map;

public final class UserDtos {
    private UserDtos() {}

    public record UserProfile(
            Long id,
            String username,
            String role,
            String status,
            List<InterestTag> interests,
            List<SimilarUser> similarUsers
    ) {}

    public record InterestTag(
            String categoryName,
            double weight
    ) {}

    public record SimilarUser(
            Long userId,
            String username,
            double similarity
    ) {}
}
