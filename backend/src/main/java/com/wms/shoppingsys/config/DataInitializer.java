package com.wms.shoppingsys.config;

import com.wms.shoppingsys.catalog.Category;
import com.wms.shoppingsys.catalog.CategoryRepository;
import com.wms.shoppingsys.catalog.Product;
import com.wms.shoppingsys.catalog.ProductRepository;
import com.wms.shoppingsys.catalog.ProductStatus;
import com.wms.shoppingsys.recommendation.BehaviorService;
import com.wms.shoppingsys.recommendation.UserBehaviorRepository;
import com.wms.shoppingsys.user.User;
import com.wms.shoppingsys.user.UserRepository;
import com.wms.shoppingsys.user.UserRole;
import com.wms.shoppingsys.user.UserStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "shopping.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final BehaviorService behaviorService;
    private final UserBehaviorRepository userBehaviorRepository;

    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository,
                           ProductRepository productRepository, BehaviorService behaviorService,
                           UserBehaviorRepository userBehaviorRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.behaviorService = behaviorService;
        this.userBehaviorRepository = userBehaviorRepository;
    }

    @Override
    public void run(String... args) {
        Map<String, User> users = seedUsers();
        Map<String, Category> categories = seedCategories();
        Map<String, Product> products = seedProducts(categories);
        seedBehaviors(users, products);
    }

    private Map<String, User> seedUsers() {
        User admin = userRepository.findByUsername("admin")
                .orElseGet(() -> userRepository.save(new User("admin", hashPassword("admin123"),
                        UserRole.ADMIN, UserStatus.ACTIVE)));
        User alice = userRepository.findByUsername("alice")
                .orElseGet(() -> userRepository.save(new User("alice", hashPassword("pass123"),
                        UserRole.USER, UserStatus.ACTIVE)));
        User bob = userRepository.findByUsername("bob")
                .orElseGet(() -> userRepository.save(new User("bob", hashPassword("pass123"),
                        UserRole.USER, UserStatus.ACTIVE)));
        User carol = userRepository.findByUsername("carol")
                .orElseGet(() -> userRepository.save(new User("carol", hashPassword("pass123"),
                        UserRole.USER, UserStatus.ACTIVE)));
        return Map.of("admin", admin, "alice", alice, "bob", bob, "carol", carol);
    }

    private Map<String, Category> seedCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    new Category("手机数码", null, true, 1),
                    new Category("电脑办公", null, true, 2),
                    new Category("家居生活", null, true, 3),
                    new Category("运动户外", null, true, 4)
            ));
        }
        List<Category> categories = categoryRepository.findAll();
        return Map.of(
                "phone", findCategory(categories, "手机数码"),
                "computer", findCategory(categories, "电脑办公"),
                "home", findCategory(categories, "家居生活"),
                "sport", findCategory(categories, "运动户外")
        );
    }

    private Map<String, Product> seedProducts(Map<String, Category> categories) {
        if (productRepository.count() == 0) {
            productRepository.saveAll(List.of(
                    product(categories.get("phone"), "aurora-phone", "Aurora Phone 15", "旗舰影像手机", "Aurora", "4999.00", 80, 320),
                    product(categories.get("phone"), "nova-phone", "Nova Mini Phone", "轻薄小屏手机", "Nova", "3299.00", 65, 210),
                    product(categories.get("phone"), "phone-case", "MagSafe Phone Case", "防摔透明手机壳", "ShellLab", "129.00", 240, 660),
                    product(categories.get("phone"), "fast-charger", "GaN Fast Charger", "65W 氮化镓快充", "Voltix", "169.00", 180, 540),
                    product(categories.get("computer"), "ultra-laptop", "FeatherBook Pro", "14 英寸高性能笔记本", "Feather", "8999.00", 36, 150),
                    product(categories.get("computer"), "mechanical-keyboard", "Tactile Mechanical Keyboard", "三模机械键盘", "KeyForge", "699.00", 90, 260),
                    product(categories.get("computer"), "wireless-mouse", "Silent Wireless Mouse", "静音办公鼠标", "Mousio", "199.00", 130, 310),
                    product(categories.get("computer"), "monitor-4k", "Vision 4K Monitor", "27 英寸 4K 显示器", "Vision", "1899.00", 42, 120),
                    product(categories.get("home"), "air-purifier", "Breeze Air Purifier", "智能空气净化器", "Breeze", "1299.00", 55, 175),
                    product(categories.get("home"), "desk-lamp", "Halo Desk Lamp", "护眼台灯", "Halo", "269.00", 110, 280),
                    product(categories.get("home"), "coffee-maker", "Morning Coffee Maker", "家用滴滤咖啡机", "Morning", "499.00", 48, 190),
                    product(categories.get("home"), "storage-box", "Modular Storage Box", "模块化收纳箱", "Nest", "89.00", 260, 420),
                    product(categories.get("sport"), "running-shoes", "CloudRun Shoes", "缓震跑步鞋", "CloudRun", "599.00", 75, 240),
                    product(categories.get("sport"), "yoga-mat", "Grip Yoga Mat", "防滑瑜伽垫", "Balance", "159.00", 160, 360),
                    product(categories.get("sport"), "bike-helmet", "Urban Bike Helmet", "城市骑行头盔", "Rider", "299.00", 70, 130),
                    product(categories.get("sport"), "wireless-headphones", "Wireless Headphones", "运动蓝牙耳机", "Pulse", "399.00", 95, 330)
            ));
        }
        List<Product> products = productRepository.findAll();
        return Map.of(
                "phone", findProduct(products, "Aurora Phone 15"),
                "case", findProduct(products, "MagSafe Phone Case"),
                "charger", findProduct(products, "GaN Fast Charger"),
                "headphones", findProduct(products, "Wireless Headphones"),
                "laptop", findProduct(products, "FeatherBook Pro"),
                "keyboard", findProduct(products, "Tactile Mechanical Keyboard"),
                "mouse", findProduct(products, "Silent Wireless Mouse")
        );
    }

    private void seedBehaviors(Map<String, User> users, Map<String, Product> products) {
        if (userBehaviorRepository.count() > 0) {
            return;
        }
        behaviorService.recordView(users.get("alice").getId(), products.get("phone").getId());
        behaviorService.recordCart(users.get("alice").getId(), products.get("case").getId());
        behaviorService.recordOrder(users.get("alice").getId(), products.get("charger").getId());

        behaviorService.recordOrder(users.get("bob").getId(), products.get("phone").getId());
        behaviorService.recordCart(users.get("bob").getId(), products.get("case").getId());
        behaviorService.recordView(users.get("bob").getId(), products.get("headphones").getId());

        behaviorService.recordOrder(users.get("carol").getId(), products.get("laptop").getId());
        behaviorService.recordCart(users.get("carol").getId(), products.get("keyboard").getId());
        behaviorService.recordView(users.get("carol").getId(), products.get("mouse").getId());
    }

    private Product product(Category category, String slug, String name, String description, String brand,
                            String price, int stock, int salesCount) {
        return new Product(category.getId(), name, description, brand, new BigDecimal(price), stock,
                "https://picsum.photos/seed/" + slug + "/640/480",
                ProductStatus.ON_SALE, salesCount);
    }

    private Category findCategory(List<Category> categories, String name) {
        return categories.stream()
                .filter(category -> name.equals(category.getName()))
                .findFirst()
                .orElseThrow();
    }

    private Product findProduct(List<Product> products, String name) {
        return products.stream()
                .filter(product -> name.equals(product.getName()))
                .findFirst()
                .orElseThrow();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
