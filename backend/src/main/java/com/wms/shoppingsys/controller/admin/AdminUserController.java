package com.wms.shoppingsys.controller.admin;

import com.wms.shoppingsys.common.ApiResponse;
import com.wms.shoppingsys.dto.UserDtos;
import com.wms.shoppingsys.entity.*;
import com.wms.shoppingsys.repository.*;
import com.wms.shoppingsys.enums.UserStatus;
import com.wms.shoppingsys.service.AuthService;
import com.wms.shoppingsys.auth.CurrentUser;
import com.wms.shoppingsys.auth.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final UserBehaviorRepository behaviorRepo;
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    // 6个品类名称
    private static final Map<Long,String> CAT_NAMES = Map.of(
        1L,"手机数码", 2L,"电脑办公", 3L,"家居生活",
        4L,"运动户外", 5L,"美妆个护", 6L,"食品饮料"
    );

    public AdminUserController(UserRepository userRepository, AuthService authService,
                               UserBehaviorRepository behaviorRepo, ProductRepository productRepo,
                               CategoryRepository categoryRepo) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.behaviorRepo = behaviorRepo;
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    @GetMapping
    public ApiResponse<List<User>> list(HttpServletRequest request) {
        authService.requireAdmin(currentUser(request));
        return ApiResponse.ok(userRepository.findAll());
    }

    @GetMapping("/profiles")
    public ApiResponse<List<UserDtos.UserProfile>> profiles(HttpServletRequest request) {
        authService.requireAdmin(currentUser(request));
        List<User> users = userRepository.findAll();
        List<UserDtos.UserProfile> profiles = new ArrayList<>();

        // 先为每个用户计算品类兴趣
        Map<Long, Map<Long,Long>> userCatWeights = new HashMap<>();
        for (User u : users) {
            Map<Long,Long> catWeights = new LinkedHashMap<>();
            for (UserBehavior b : behaviorRepo.findByUserId(u.getId())) {
                productRepo.findById(b.getProductId()).ifPresent(p -> {
                    catWeights.merge(p.getCategoryId(), (long)b.getWeight(), Long::sum);
                });
            }
            userCatWeights.put(u.getId(), catWeights);
        }

        // 为每个用户生成兴趣标签和相似用户
        for (User u : users) {
            Map<Long,Long> catWeights = userCatWeights.get(u.getId());
            List<UserDtos.InterestTag> tags = catWeights.entrySet().stream()
                .sorted(Map.Entry.<Long,Long>comparingByValue().reversed())
                .limit(3)
                .map(e -> new UserDtos.InterestTag(
                    CAT_NAMES.getOrDefault(e.getKey(), "其他"), e.getValue()/1.0))
                .collect(Collectors.toList());

            // 计算相似用户（根据品类兴趣重叠度）
            List<UserDtos.SimilarUser> similar = new ArrayList<>();
            for (User other : users) {
                if (other.getId().equals(u.getId())) continue;
                Map<Long,Long> otherWeights = userCatWeights.get(other.getId());
                double sim = cosineSimilarity(catWeights, otherWeights);
                if (sim > 0.15) {
                    similar.add(new UserDtos.SimilarUser(other.getId(), other.getUsername(), sim));
                }
            }
            similar.sort((a,b) -> Double.compare(b.similarity(), a.similarity()));
            if (similar.size() > 3) similar = similar.subList(0, 3);

            profiles.add(new UserDtos.UserProfile(
                u.getId(), u.getUsername(), u.getRole().name(), u.getStatus().name(),
                tags, similar));
        }

        return ApiResponse.ok(profiles);
    }

    // 余弦相似度（基于品类权重向量）
    private double cosineSimilarity(Map<Long,Long> a, Map<Long,Long> b) {
        Set<Long> keys = new HashSet<>(a.keySet());
        keys.addAll(b.keySet());
        double dot = 0, normA = 0, normB = 0;
        for (Long k : keys) {
            double va = a.getOrDefault(k, 0L).doubleValue();
            double vb = b.getOrDefault(k, 0L).doubleValue();
            dot += va * vb; normA += va * va; normB += vb * vb;
        }
        if (normA == 0 || normB == 0) return 0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
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
