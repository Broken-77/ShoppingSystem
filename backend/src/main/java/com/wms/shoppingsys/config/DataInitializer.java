package com.wms.shoppingsys.config;

import com.wms.shoppingsys.entity.Category;
import com.wms.shoppingsys.repository.CategoryRepository;
import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.repository.ProductRepository;
import com.wms.shoppingsys.enums.ProductStatus;
import com.wms.shoppingsys.service.BehaviorService;
import com.wms.shoppingsys.repository.UserBehaviorRepository;
import com.wms.shoppingsys.entity.User;
import com.wms.shoppingsys.repository.UserRepository;
import com.wms.shoppingsys.enums.UserRole;
import com.wms.shoppingsys.enums.UserStatus;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

    private static final java.util.Map<String, String> IMG = new java.util.HashMap<>();

    static {
        IMG.put("iPhone 15 Pro Max", "https://images.pexels.com/photos/3945672/pexels-photo-3945672.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 Mate 60 Pro", "https://images.pexels.com/photos/33118984/pexels-photo-33118984.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 14 Ultra", "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop");
        IMG.put("OPPO Find X7 Ultra", "https://images.pexels.com/photos/20074768/pexels-photo-20074768.jpeg?w=400&h=300&fit=crop");
        IMG.put("vivo X100 Pro", "https://images.pexels.com/photos/35229446/pexels-photo-35229446.jpeg?w=400&h=300&fit=crop");
        IMG.put("三星 Galaxy S24 Ultra", "https://images.pexels.com/photos/47261/pexels-photo-47261.jpeg?w=400&h=300&fit=crop");
        IMG.put("一加 12", "https://images.pexels.com/photos/10343713/pexels-photo-10343713.jpeg?w=400&h=300&fit=crop");
        IMG.put("荣耀 Magic6 Pro", "https://images.pexels.com/photos/34391717/pexels-photo-34391717.jpeg?w=400&h=300&fit=crop");
        IMG.put("Nova 12 Ultra", "https://images.pexels.com/photos/33118984/pexels-photo-33118984.jpeg?w=400&h=300&fit=crop");
        IMG.put("AirPods Pro 2", "https://images.pexels.com/photos/3921845/pexels-photo-3921845.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 FreeBuds Pro 3", "https://images.pexels.com/photos/14741306/pexels-photo-14741306.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 Buds 4 Pro", "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop");
        IMG.put("Apple Watch Ultra 2", "https://images.pexels.com/photos/5083218/pexels-photo-5083218.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 Watch GT 4", "https://images.pexels.com/photos/5083218/pexels-photo-5083218.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米手环 8 Pro", "https://images.pexels.com/photos/6516206/pexels-photo-6516206.jpeg?w=400&h=300&fit=crop");
        IMG.put("iPad Air M2", "https://images.pexels.com/photos/10535365/pexels-photo-10535365.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 MatePad Pro 13.2", "https://images.pexels.com/photos/33118984/pexels-photo-33118984.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米平板 6S Pro", "https://images.pexels.com/photos/6913311/pexels-photo-6913311.jpeg?w=400&h=300&fit=crop");
        IMG.put("ANKER 65W 氮化镓充电器", "https://images.pexels.com/photos/1028674/pexels-photo-1028674.jpeg?w=400&h=300&fit=crop");
        IMG.put("闪迪 256GB TF 存储卡", "https://images.pexels.com/photos/2147082/pexels-photo-2147082.jpeg?w=400&h=300&fit=crop");
        IMG.put("绿联 手机支架 桌面", "https://images.pexels.com/photos/34882934/pexels-photo-34882934.jpeg?w=400&h=300&fit=crop");
        IMG.put("大疆 Osmo Mobile 6", "https://images.pexels.com/photos/6732203/pexels-photo-6732203.jpeg?w=400&h=300&fit=crop");
        IMG.put("Sony WH-1000XM5", "https://images.pexels.com/photos/37668074/pexels-photo-37668074.jpeg?w=400&h=300&fit=crop");
        IMG.put("Bose QC Ultra", "https://images.pexels.com/photos/29617989/pexels-photo-29617989.jpeg?w=400&h=300&fit=crop");
        IMG.put("JBL Flip 6", "https://images.pexels.com/photos/20285556/pexels-photo-20285556.jpeg?w=400&h=300&fit=crop");
        IMG.put("Kindle Paperwhite 5", "https://images.pexels.com/photos/18313245/pexels-photo-18313245.jpeg?w=400&h=300&fit=crop");
        IMG.put("GoPro Hero 12 Black", "https://images.pexels.com/photos/8799699/pexels-photo-8799699.jpeg?w=400&h=300&fit=crop");
        IMG.put("Insta360 X4", "https://images.pexels.com/photos/6630001/pexels-photo-6630001.jpeg?w=400&h=300&fit=crop");
        IMG.put("MacBook Pro 14 M3", "https://images.pexels.com/photos/10655906/pexels-photo-10655906.jpeg?w=400&h=300&fit=crop");
        IMG.put("ThinkPad X1 Carbon Gen 12", "https://images.pexels.com/photos/3550482/pexels-photo-3550482.jpeg?w=400&h=300&fit=crop");
        IMG.put("华硕 ROG 枪神8", "https://images.pexels.com/photos/12877878/pexels-photo-12877878.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 MateBook X Pro 2024", "https://images.pexels.com/photos/33118984/pexels-photo-33118984.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 Book Pro 16", "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop");
        IMG.put("Dell U2724D", "https://images.pexels.com/photos/9539073/pexels-photo-9539073.jpeg?w=400&h=300&fit=crop");
        IMG.put("LG 27GR95UM", "https://images.pexels.com/photos/31726554/pexels-photo-31726554.jpeg?w=400&h=300&fit=crop");
        IMG.put("Samsung ViewFinity S9", "https://images.pexels.com/photos/214488/pexels-photo-214488.jpeg?w=400&h=300&fit=crop");
        IMG.put("Logitech MX Master 3S", "https://images.pexels.com/photos/7006949/pexels-photo-7006949.jpeg?w=400&h=300&fit=crop");
        IMG.put("雷蛇 DeathAdder V3 Pro", "https://images.pexels.com/photos/30641697/pexels-photo-30641697.jpeg?w=400&h=300&fit=crop");
        IMG.put("Keychron Q1 Pro", "https://images.pexels.com/photos/28534977/pexels-photo-28534977.jpeg?w=400&h=300&fit=crop");
        IMG.put("Cherry MX Board 3.0S", "https://images.pexels.com/photos/8180524/pexels-photo-8180524.jpeg?w=400&h=300&fit=crop");
        IMG.put("罗技 G Pro X 耳机", "https://images.pexels.com/photos/210927/pexels-photo-210927.jpeg?w=400&h=300&fit=crop");
        IMG.put("Blue Yeti X USB 麦克风", "https://images.pexels.com/photos/37976280/pexels-photo-37976280.jpeg?w=400&h=300&fit=crop");
        IMG.put("Samsung T7 Shield 2TB", "https://images.pexels.com/photos/214488/pexels-photo-214488.jpeg?w=400&h=300&fit=crop");
        IMG.put("WD My Book 12TB", "https://images.pexels.com/photos/10182867/pexels-photo-10182867.jpeg?w=400&h=300&fit=crop");
        IMG.put("群晖 DS224+", "https://images.pexels.com/photos/37283954/pexels-photo-37283954.jpeg?w=400&h=300&fit=crop");
        IMG.put("TP-Link AXE5400", "https://images.pexels.com/photos/18446979/pexels-photo-18446979.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 AX6", "https://images.pexels.com/photos/19999076/pexels-photo-19999076.jpeg?w=400&h=300&fit=crop");
        IMG.put("明基 ScreenBar Halo", "https://images.pexels.com/photos/34619007/pexels-photo-34619007.jpeg?w=400&h=300&fit=crop");
        IMG.put("爱格升 LX 显示器支架", "https://images.pexels.com/photos/326512/pexels-photo-326512.jpeg?w=400&h=300&fit=crop");
        IMG.put("Herman Miller Aeron", "https://images.pexels.com/photos/3968051/pexels-photo-3968051.jpeg?w=400&h=300&fit=crop");
        IMG.put("网易严选 人体工学椅", "https://images.pexels.com/photos/31726674/pexels-photo-31726674.jpeg?w=400&h=300&fit=crop");
        IMG.put("Wacom Intuos Pro M", "https://images.pexels.com/photos/301792/pexels-photo-301792.jpeg?w=400&h=300&fit=crop");
        IMG.put("罗技 StreamCam", "https://images.pexels.com/photos/35078392/pexels-photo-35078392.jpeg?w=400&h=300&fit=crop");
        IMG.put("APC Back-UPS 650", "https://images.pexels.com/photos/11349464/pexels-photo-11349464.jpeg?w=400&h=300&fit=crop");
        IMG.put("绿联 雷电4 扩展坞", "https://images.pexels.com/photos/34882934/pexels-photo-34882934.jpeg?w=400&h=300&fit=crop");
        IMG.put("微软 Surface Pro 10", "https://images.pexels.com/photos/1181207/pexels-photo-1181207.jpeg?w=400&h=300&fit=crop");
        IMG.put("戴森 V15 Detect", "https://images.pexels.com/photos/36038694/pexels-photo-36038694.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 扫拖机器人 X20+", "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop");
        IMG.put("戴森 Airwrap HS05", "https://images.pexels.com/photos/36038694/pexels-photo-36038694.jpeg?w=400&h=300&fit=crop");
        IMG.put("飞利浦 空气炸锅 HD9651", "https://images.pexels.com/photos/35285814/pexels-photo-35285814.jpeg?w=400&h=300&fit=crop");
        IMG.put("德龙 ECAM23.460 全自动咖啡机", "https://images.pexels.com/photos/6612594/pexels-photo-6612594.jpeg?w=400&h=300&fit=crop");
        IMG.put("松下 SR-HBC184 电饭煲", "https://images.pexels.com/photos/25242875/pexels-photo-25242875.jpeg?w=400&h=300&fit=crop");
        IMG.put("九阳 DJ13B-D08EC 豆浆机", "https://images.pexels.com/photos/35261867/pexels-photo-35261867.jpeg?w=400&h=300&fit=crop");
        IMG.put("Bose SoundLink Max", "https://images.pexels.com/photos/29617989/pexels-photo-29617989.jpeg?w=400&h=300&fit=crop");
        IMG.put("戴森 Pure Cool 空气净化风扇", "https://images.pexels.com/photos/36038694/pexels-photo-36038694.jpeg?w=400&h=300&fit=crop");
        IMG.put("Aqara 智能窗帘电机 C3", "https://images.pexels.com/photos/25473948/pexels-photo-25473948.jpeg?w=400&h=300&fit=crop");
        IMG.put("Yeelight 智能吸顶灯 Pro", "https://images.pexels.com/photos/19701594/pexels-photo-19701594.jpeg?w=400&h=300&fit=crop");
        IMG.put("德业 DYD-T22A3 除湿机", "https://images.pexels.com/photos/34314973/pexels-photo-34314973.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 空气净化器 4 Pro", "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop");
        IMG.put("MUJI 超声波香薰机", "https://images.pexels.com/photos/26732084/pexels-photo-26732084.jpeg?w=400&h=300&fit=crop");
        IMG.put("象印 SM-SG48 保温杯", "https://images.pexels.com/photos/10361760/pexels-photo-10361760.jpeg?w=400&h=300&fit=crop");
        IMG.put("水星家纺 95鹅绒被", "https://images.pexels.com/photos/2344867/pexels-photo-2344867.jpeg?w=400&h=300&fit=crop");
        IMG.put("天堂 晴雨伞 三折", "https://images.pexels.com/photos/29725599/pexels-photo-29725599.jpeg?w=400&h=300&fit=crop");
        IMG.put("3M 净水器 SDW8000", "https://images.pexels.com/photos/3992946/pexels-photo-3992946.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 电热水壶 2", "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop");
        IMG.put("Bruno 多功能料理锅", "https://images.pexels.com/photos/36988958/pexels-photo-36988958.jpeg?w=400&h=300&fit=crop");
        IMG.put("美的 M1-L213C 微波炉", "https://images.pexels.com/photos/31570615/pexels-photo-31570615.jpeg?w=400&h=300&fit=crop");
        IMG.put("Nike Air Zoom Pegasus 40", "https://images.pexels.com/photos/2043476/pexels-photo-2043476.jpeg?w=400&h=300&fit=crop");
        IMG.put("Adidas Ultraboost Light", "https://images.pexels.com/photos/11883282/pexels-photo-11883282.jpeg?w=400&h=300&fit=crop");
        IMG.put("李宁 超轻21 跑鞋", "https://images.pexels.com/photos/260044/pexels-photo-260044.jpeg?w=400&h=300&fit=crop");
        IMG.put("安踏 C37 5.0 休闲鞋", "https://images.pexels.com/photos/15059764/pexels-photo-15059764.jpeg?w=400&h=300&fit=crop");
        IMG.put("Lululemon Align 瑜伽裤", "https://images.pexels.com/photos/6246682/pexels-photo-6246682.jpeg?w=400&h=300&fit=crop");
        IMG.put("Keep 瑜伽垫 TPE", "https://images.pexels.com/photos/6246682/pexels-photo-6246682.jpeg?w=400&h=300&fit=crop");
        IMG.put("Nike Pro 运动内衣", "https://images.pexels.com/photos/2043476/pexels-photo-2043476.jpeg?w=400&h=300&fit=crop");
        IMG.put("迪卡侬 MH500 登山包", "https://images.pexels.com/photos/1178525/pexels-photo-1178525.jpeg?w=400&h=300&fit=crop");
        IMG.put("The North Face 羽绒服 96Nuptse", "https://images.pexels.com/photos/37550191/pexels-photo-37550191.jpeg?w=400&h=300&fit=crop");
        IMG.put("骆驼 露营帐篷 3-4人", "https://images.pexels.com/photos/17192955/pexels-photo-17192955.jpeg?w=400&h=300&fit=crop");
        IMG.put("Stanley 户外保温箱 15L", "https://images.pexels.com/photos/14974681/pexels-photo-14974681.jpeg?w=400&h=300&fit=crop");
        IMG.put("YETI Rambler 水壶 769ml", "https://images.pexels.com/photos/8266854/pexels-photo-8266854.jpeg?w=400&h=300&fit=crop");
        IMG.put("Peloton Bike+ 健身车", "https://images.pexels.com/photos/24244151/pexels-photo-24244151.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 走步机 C2", "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop");
        IMG.put("Keep 智能跳绳", "https://images.pexels.com/photos/4108295/pexels-photo-4108295.jpeg?w=400&h=300&fit=crop");
        IMG.put("哑铃套装 20kg 可调节", "https://images.pexels.com/photos/7743320/pexels-photo-7743320.jpeg?w=400&h=300&fit=crop");
        IMG.put("Nike 瑜伽训练垫 5mm", "https://images.pexels.com/photos/6246682/pexels-photo-6246682.jpeg?w=400&h=300&fit=crop");
        IMG.put("Garmin Fenix 7X Pro", "https://images.pexels.com/photos/3999644/pexels-photo-3999644.jpeg?w=400&h=300&fit=crop");
        IMG.put("佳明 Forerunner 265", "https://images.pexels.com/photos/37894081/pexels-photo-37894081.jpeg?w=400&h=300&fit=crop");
        IMG.put("Shimano RC7 骑行锁鞋", "https://images.pexels.com/photos/5807638/pexels-photo-5807638.jpeg?w=400&h=300&fit=crop");
        IMG.put("崔克 Domane AL 4", "https://images.pexels.com/photos/14375828/pexels-photo-14375828.jpeg?w=400&h=300&fit=crop");
        IMG.put("Speedo Fastskin 泳镜", "https://images.pexels.com/photos/28939350/pexels-photo-28939350.jpeg?w=400&h=300&fit=crop");
        IMG.put("Arena 游泳训练脚蹼", "https://images.pexels.com/photos/30157150/pexels-photo-30157150.jpeg?w=400&h=300&fit=crop");
        IMG.put("Wilson NBA 篮球", "https://images.pexels.com/photos/13179883/pexels-photo-13179883.jpeg?w=400&h=300&fit=crop");
        IMG.put("Yonex 天斧 AX100ZZ", "https://images.pexels.com/photos/11312132/pexels-photo-11312132.jpeg?w=400&h=300&fit=crop");
        IMG.put("Garmin Edge 540 码表", "https://images.pexels.com/photos/3999644/pexels-photo-3999644.jpeg?w=400&h=300&fit=crop");
        IMG.put("兰蔻 极光水 150ml", "https://images.pexels.com/photos/19999076/pexels-photo-19999076.jpeg?w=400&h=300&fit=crop");
        IMG.put("SK-II 神仙水 230ml", "https://images.pexels.com/photos/10476733/pexels-photo-10476733.jpeg?w=400&h=300&fit=crop");
        IMG.put("雅诗兰黛 小棕瓶精华 50ml", "https://images.pexels.com/photos/7321654/pexels-photo-7321654.jpeg?w=400&h=300&fit=crop");
        IMG.put("海蓝之谜 面霜 60ml", "https://images.pexels.com/photos/6635929/pexels-photo-6635929.jpeg?w=400&h=300&fit=crop");
        IMG.put("Olay 小白瓶 40ml", "https://images.pexels.com/photos/28847445/pexels-photo-28847445.jpeg?w=400&h=300&fit=crop");
        IMG.put("珀莱雅 双抗精华 30ml", "https://images.pexels.com/photos/7321654/pexels-photo-7321654.jpeg?w=400&h=300&fit=crop");
        IMG.put("迪奥 口红 999 哑光", "https://images.pexels.com/photos/6648498/pexels-photo-6648498.jpeg?w=400&h=300&fit=crop");
        IMG.put("Tom Ford 黑金唇膏 16", "https://images.pexels.com/photos/5809488/pexels-photo-5809488.jpeg?w=400&h=300&fit=crop");
        IMG.put("Charlotte Tilbury 枕边话", "https://images.pexels.com/photos/7466181/pexels-photo-7466181.jpeg?w=400&h=300&fit=crop");
        IMG.put("YSL 小金条 #21", "https://images.pexels.com/photos/29899584/pexels-photo-29899584.jpeg?w=400&h=300&fit=crop");
        IMG.put("MAC 生姜高光 DoubleGleam", "https://images.pexels.com/photos/39284/pexels-photo-39284.jpeg?w=400&h=300&fit=crop");
        IMG.put("Jo Malone 蓝风铃 30ml", "https://images.pexels.com/photos/33615818/pexels-photo-33615818.jpeg?w=400&h=300&fit=crop");
        IMG.put("Diptyque 檀道 75ml", "https://images.pexels.com/photos/2866796/pexels-photo-2866796.jpeg?w=400&h=300&fit=crop");
        IMG.put("潘海利根 兽首狐狸 75ml", "https://images.pexels.com/photos/7610571/pexels-photo-7610571.jpeg?w=400&h=300&fit=crop");
        IMG.put("Aesop 赋活芳香护手霜 75ml", "https://images.pexels.com/photos/17788297/pexels-photo-17788297.jpeg?w=400&h=300&fit=crop");
        IMG.put("卡诗 白金赋活洗发水 250ml", "https://images.pexels.com/photos/33807622/pexels-photo-33807622.jpeg?w=400&h=300&fit=crop");
        IMG.put("戴森 Supersonic 吹风机", "https://images.pexels.com/photos/36038694/pexels-photo-36038694.jpeg?w=400&h=300&fit=crop");
        IMG.put("Marvis 牙膏套装 7支", "https://images.pexels.com/photos/3735649/pexels-photo-3735649.jpeg?w=400&h=300&fit=crop");
        IMG.put("CeraVe 保湿洁面乳 473ml", "https://images.pexels.com/photos/14107358/pexels-photo-14107358.jpeg?w=400&h=300&fit=crop");
        IMG.put("修丽可 CF 抗氧化精华 30ml", "https://images.pexels.com/photos/7321654/pexels-photo-7321654.jpeg?w=400&h=300&fit=crop");
        IMG.put("三只松鼠 每日坚果 750g", "https://images.pexels.com/photos/5507656/pexels-photo-5507656.jpeg?w=400&h=300&fit=crop");
        IMG.put("良品铺子 猪肉脯 500g", "https://images.pexels.com/photos/5237009/pexels-photo-5237009.jpeg?w=400&h=300&fit=crop");
        IMG.put("瑞士莲 软心巧克力 600g", "https://images.pexels.com/photos/6167328/pexels-photo-6167328.jpeg?w=400&h=300&fit=crop");
        IMG.put("illy 深度烘焙咖啡粉 250g", "https://images.pexels.com/photos/27528587/pexels-photo-27528587.jpeg?w=400&h=300&fit=crop");
        IMG.put("瑞幸 冻干咖啡粉 12颗", "https://images.pexels.com/photos/5574070/pexels-photo-5574070.jpeg?w=400&h=300&fit=crop");
        IMG.put("永璞 闪萃咖啡液 14杯", "https://images.pexels.com/photos/19999076/pexels-photo-19999076.jpeg?w=400&h=300&fit=crop");
        IMG.put("认养一头牛 纯牛奶 250ml×24盒", "https://images.pexels.com/photos/12420819/pexels-photo-12420819.jpeg?w=400&h=300&fit=crop");
        IMG.put("安佳 全脂奶粉 1kg", "https://images.pexels.com/photos/37424061/pexels-photo-37424061.jpeg?w=400&h=300&fit=crop");
        IMG.put("北海牧场 酸奶 200g×12杯", "https://images.pexels.com/photos/4641957/pexels-photo-4641957.jpeg?w=400&h=300&fit=crop");
        IMG.put("桂格 即食燕麦片 1.5kg", "https://images.pexels.com/photos/30501878/pexels-photo-30501878.jpeg?w=400&h=300&fit=crop");
        IMG.put("汤臣倍健 蛋白粉 450g", "https://images.pexels.com/photos/5904234/pexels-photo-5904234.jpeg?w=400&h=300&fit=crop");
        IMG.put("东方树叶 茉莉花茶 500ml×15", "https://images.pexels.com/photos/34945089/pexels-photo-34945089.jpeg?w=400&h=300&fit=crop");
        IMG.put("农夫山泉 矿泉水 550ml×24", "https://images.pexels.com/photos/6391524/pexels-photo-6391524.jpeg?w=400&h=300&fit=crop");
        IMG.put("青岛啤酒 经典1903 500ml×12", "https://images.pexels.com/photos/5537952/pexels-photo-5537952.jpeg?w=400&h=300&fit=crop");
        IMG.put("奔富 Bin 389 干红 750ml", "https://images.pexels.com/photos/16055974/pexels-photo-16055974.jpeg?w=400&h=300&fit=crop");
        IMG.put("锐澳 微醺鸡尾酒 330ml×8", "https://images.pexels.com/photos/19999076/pexels-photo-19999076.jpeg?w=400&h=300&fit=crop");
        IMG.put("茅台 飞天53度 500ml", "https://images.pexels.com/photos/30770256/pexels-photo-30770256.jpeg?w=400&h=300&fit=crop");
        IMG.put("奈雪的茶 茉莉初雪 茶包 15袋", "https://images.pexels.com/photos/6391524/pexels-photo-6391524.jpeg?w=400&h=300&fit=crop");
        IMG.put("小猪呵呵 午餐肉 340g", "https://images.pexels.com/photos/34945089/pexels-photo-34945089.jpeg?w=400&h=300&fit=crop");
        IMG.put("沃隆 每日坚果 礼盒装 1kg", "https://images.pexels.com/photos/5507656/pexels-photo-5507656.jpeg?w=400&h=300&fit=crop");
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
        User dave = userRepository.findByUsername("dave")
                .orElseGet(() -> userRepository.save(new User("dave", hashPassword("pass123"),
                        UserRole.USER, UserStatus.ACTIVE)));
        User eve = userRepository.findByUsername("eve")
                .orElseGet(() -> userRepository.save(new User("eve", hashPassword("pass123"),
                        UserRole.USER, UserStatus.ACTIVE)));
        User frank = userRepository.findByUsername("frank")
                .orElseGet(() -> userRepository.save(new User("frank", hashPassword("pass123"),
                        UserRole.USER, UserStatus.ACTIVE)));
        return Map.of("admin", admin, "alice", alice, "bob", bob, "carol", carol,
                "dave", dave, "eve", eve, "frank", frank);
    }

    private Map<String, Category> seedCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(List.of(
                    new Category("手机数码", null, true, 1),
                    new Category("电脑办公", null, true, 2),
                    new Category("家居生活", null, true, 3),
                    new Category("运动户外", null, true, 4),
                    new Category("美妆个护", null, true, 5),
                    new Category("食品饮料", null, true, 6)
            ));
        } else if (categoryRepository.count() < 6) {
            // 兼容旧数据库：补全缺少的分类
            List<Category> existing = categoryRepository.findAll();
            if (findCategoryOpt(existing, "美妆个护") == null) {
                categoryRepository.save(new Category("美妆个护", null, true, 5));
            }
            if (findCategoryOpt(existing, "食品饮料") == null) {
                categoryRepository.save(new Category("食品饮料", null, true, 6));
            }
        }
        List<Category> categories = categoryRepository.findAll();
        return Map.of(
                "phone", findCategory(categories, "手机数码"),
                "computer", findCategory(categories, "电脑办公"),
                "home", findCategory(categories, "家居生活"),
                "sport", findCategory(categories, "运动户外"),
                "beauty", findCategory(categories, "美妆个护"),
                "food", findCategory(categories, "食品饮料")
        );
    }

    private Category findCategoryOpt(List<Category> categories, String name) {
        return categories.stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .orElse(null);
    }

    private Map<String, Product> seedProducts(Map<String, Category> categories) {
        if (productRepository.count() == 0) {
            // 手机数码 (38件)
            productRepository.save(new Product(categories.get("phone").getId(), "iPhone 15 Pro Max", "iPhone 15 Pro Max", "Brand", new BigDecimal("7666"), 39, "https://images.pexels.com/photos/3945672/pexels-photo-3945672.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 79));
            productRepository.save(new Product(categories.get("phone").getId(), "华为 Mate 60 Pro", "华为 Mate 60 Pro", "Brand", new BigDecimal("3923.9"), 21, "https://images.pexels.com/photos/33118984/pexels-photo-33118984.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 82));
            productRepository.save(new Product(categories.get("phone").getId(), "小米 14 Ultra", "小米 14 Ultra", "Brand", new BigDecimal("7935"), 26, "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 68));
            productRepository.save(new Product(categories.get("phone").getId(), "OPPO Find X7 Ultra", "OPPO Find X7 Ultra", "Brand", new BigDecimal("8337.8"), 50, "https://images.pexels.com/photos/20074768/pexels-photo-20074768.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 79));
            productRepository.save(new Product(categories.get("phone").getId(), "vivo X100 Pro", "vivo X100 Pro", "Brand", new BigDecimal("8961.9"), 24, "https://images.pexels.com/photos/35229446/pexels-photo-35229446.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 44));
            productRepository.save(new Product(categories.get("phone").getId(), "三星 Galaxy S24 Ultra", "三星 Galaxy S24 Ultra", "Brand", new BigDecimal("8207"), 27, "https://images.pexels.com/photos/47261/pexels-photo-47261.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 41));
            productRepository.save(new Product(categories.get("phone").getId(), "一加 12", "一加 12", "Brand", new BigDecimal("8002"), 15, "https://images.pexels.com/photos/10343713/pexels-photo-10343713.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 210));
            productRepository.save(new Product(categories.get("phone").getId(), "荣耀 Magic6 Pro", "荣耀 Magic6 Pro", "Brand", new BigDecimal("4611.8"), 36, "https://images.pexels.com/photos/34391717/pexels-photo-34391717.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 92));
            productRepository.save(new Product(categories.get("phone").getId(), "Nova 12 Ultra", "Nova 12 Ultra", "Brand", new BigDecimal("8104"), 38, "https://img2.baidu.com/it/u=1604138311,3450159460&fm=253&app=138&f=JPEG?w=800&h=1067", ProductStatus.ON_SALE, 112));
            productRepository.save(new Product(categories.get("phone").getId(), "AirPods Pro 2", "AirPods Pro 2", "Brand", new BigDecimal("5462"), 19, "https://images.pexels.com/photos/3921845/pexels-photo-3921845.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 265));
            productRepository.save(new Product(categories.get("phone").getId(), "华为 FreeBuds Pro 3", "华为 FreeBuds Pro 3", "Brand", new BigDecimal("563.9"), 46, "https://images.pexels.com/photos/14741306/pexels-photo-14741306.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 258));
            productRepository.save(new Product(categories.get("phone").getId(), "小米 Buds 4 Pro", "小米 Buds 4 Pro", "Brand", new BigDecimal("1924"), 57, "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 339));
            productRepository.save(new Product(categories.get("phone").getId(), "Apple Watch Ultra 2", "Apple Watch Ultra 2", "Brand", new BigDecimal("8583"), 14, "https://images.pexels.com/photos/5083218/pexels-photo-5083218.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 33));
            productRepository.save(new Product(categories.get("phone").getId(), "华为 Watch GT 4", "华为 Watch GT 4", "Brand", new BigDecimal("630.9"), 31, "https://img0.baidu.com/it/u=2431263106,894007510&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1410", ProductStatus.ON_SALE, 314));
            productRepository.save(new Product(categories.get("phone").getId(), "小米手环 8 Pro", "小米手环 8 Pro", "Brand", new BigDecimal("1853"), 88, "https://img0.baidu.com/it/u=3372477375,240718339&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=653", ProductStatus.ON_SALE, 241));
            productRepository.save(new Product(categories.get("phone").getId(), "iPad Air M2", "iPad Air M2", "Brand", new BigDecimal("3819.8"), 23, "https://images.pexels.com/photos/10535365/pexels-photo-10535365.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 211));
            productRepository.save(new Product(categories.get("phone").getId(), "华为 MatePad Pro 13.2", "华为 MatePad Pro 13.2", "Brand", new BigDecimal("2818"), 30, "https://images.pexels.com/photos/33118984/pexels-photo-33118984.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 107));
            productRepository.save(new Product(categories.get("phone").getId(), "小米平板 6S Pro", "小米平板 6S Pro", "Brand", new BigDecimal("1889"), 31, "https://images.pexels.com/photos/6913311/pexels-photo-6913311.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 92));
            productRepository.save(new Product(categories.get("phone").getId(), "ANKER 65W 氮化镓充电器", "ANKER 65W 氮化镓充电器", "Brand", new BigDecimal("2031"), 51, "https://images.pexels.com/photos/1028674/pexels-photo-1028674.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 173));
            productRepository.save(new Product(categories.get("phone").getId(), "闪迪 256GB TF 存储卡", "闪迪 256GB TF 存储卡", "Brand", new BigDecimal("8528.5"), 125, "https://img2.baidu.com/it/u=4160309347,225796644&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=840", ProductStatus.ON_SALE, 758));
            productRepository.save(new Product(categories.get("phone").getId(), "Sony WH-1000XM5", "Sony WH-1000XM5", "Brand", new BigDecimal("3108.5"), 17, "https://images.pexels.com/photos/37668074/pexels-photo-37668074.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 198));
            productRepository.save(new Product(categories.get("phone").getId(), "Bose QC Ultra", "Bose QC Ultra", "Brand", new BigDecimal("7275.5"), 30, "https://images.pexels.com/photos/29617989/pexels-photo-29617989.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 188));
            productRepository.save(new Product(categories.get("phone").getId(), "JBL Flip 6", "JBL Flip 6", "Brand", new BigDecimal("3344.9"), 22, "https://images.pexels.com/photos/20285556/pexels-photo-20285556.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 267));
            productRepository.save(new Product(categories.get("phone").getId(), "Kindle Paperwhite 5", "Kindle Paperwhite 5", "Brand", new BigDecimal("8884.9"), 116, "https://img0.baidu.com/it/u=4107566279,1156054415&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 200));
            productRepository.save(new Product(categories.get("phone").getId(), "GoPro Hero 12 Black", "GoPro Hero 12 Black", "Brand", new BigDecimal("6981"), 38, "https://images.pexels.com/photos/8799699/pexels-photo-8799699.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 384));
            productRepository.save(new Product(categories.get("phone").getId(), "Insta360 X4", "Insta360 X4", "Brand", new BigDecimal("8694.9"), 20, "https://images.pexels.com/photos/6630001/pexels-photo-6630001.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 257));
            productRepository.save(new Product(categories.get("phone").getId(), "MacBook Pro 14 M3", "MacBook Pro 14 M3", "Brand", new BigDecimal("6675"), 18, "https://images.pexels.com/photos/10655906/pexels-photo-10655906.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 29));
            productRepository.save(new Product(categories.get("phone").getId(), "华为 MateBook X Pro 2024", "华为 MateBook X Pro 2024", "Brand", new BigDecimal("3942.5"), 11, "https://img2.baidu.com/it/u=3832484179,1808499694&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667", ProductStatus.ON_SALE, 137));
            productRepository.save(new Product(categories.get("phone").getId(), "小米 Book Pro 16", "小米 Book Pro 16", "Brand", new BigDecimal("1079"), 11, "https://img1.baidu.com/it/u=1237521389,3022206281&fm=253&fmt=auto&app=120&f=JPEG?w=887&h=800", ProductStatus.ON_SALE, 95));
            productRepository.save(new Product(categories.get("phone").getId(), "Samsung ViewFinity S9", "Samsung ViewFinity S9", "Brand", new BigDecimal("8180.9"), 28, "https://images.pexels.com/photos/214488/pexels-photo-214488.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 54));
            productRepository.save(new Product(categories.get("phone").getId(), "罗技 G Pro X 耳机", "罗技 G Pro X 耳机", "Brand", new BigDecimal("1182.9"), 49, "https://images.pexels.com/photos/210927/pexels-photo-210927.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 177));
            productRepository.save(new Product(categories.get("phone").getId(), "Samsung T7 Shield 2TB", "Samsung T7 Shield 2TB", "Brand", new BigDecimal("4030.9"), 114, "https://img2.baidu.com/it/u=1699828447,2848501117&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=584", ProductStatus.ON_SALE, 447));
            productRepository.save(new Product(categories.get("phone").getId(), "华为 AX6", "华为 AX6", "Brand", new BigDecimal("1452.9"), 119, "https://img0.baidu.com/it/u=2077417276,3289627424&fm=253&fmt=auto?w=640&h=677", ProductStatus.ON_SALE, 454));
            productRepository.save(new Product(categories.get("phone").getId(), "小米 扫拖机器人 X20+", "小米 扫拖机器人 X20+", "Brand", new BigDecimal("4696"), 37, "https://img1.baidu.com/it/u=3593545207,3113610742&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500", ProductStatus.ON_SALE, 88));
            productRepository.save(new Product(categories.get("phone").getId(), "Bose SoundLink Max", "Bose SoundLink Max", "Brand", new BigDecimal("523"), 148, "https://img1.baidu.com/it/u=3581404649,53606868&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=750", ProductStatus.ON_SALE, 481));
            productRepository.save(new Product(categories.get("phone").getId(), "小米 空气净化器 4 Pro", "小米 空气净化器 4 Pro", "Brand", new BigDecimal("2568"), 15, "https://img1.baidu.com/it/u=1909335717,36263449&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 139));
            productRepository.save(new Product(categories.get("phone").getId(), "小米 电热水壶 2", "小米 电热水壶 2", "Brand", new BigDecimal("3022.9"), 74, "https://img1.baidu.com/it/u=3932252322,2859413546&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=967", ProductStatus.ON_SALE, 416));
            productRepository.save(new Product(categories.get("phone").getId(), "小米 走步机 C2", "小米 走步机 C2", "Brand", new BigDecimal("208"), 81, "https://img2.baidu.com/it/u=2895369781,3859827330&fm=253&fmt=auto&app=120&f=JPEG?w=760&h=760", ProductStatus.ON_SALE, 182));
            // 电脑办公 (19件)
            productRepository.save(new Product(categories.get("computer").getId(), "绿联 手机支架 桌面", "绿联 手机支架 桌面", "Brand", new BigDecimal("5349.5"), 72, "https://img0.baidu.com/it/u=514960464,4207606464&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=888", ProductStatus.ON_SALE, 149));
            productRepository.save(new Product(categories.get("computer").getId(), "ThinkPad X1 Carbon Gen 12", "ThinkPad X1 Carbon Gen 12", "Brand", new BigDecimal("12001.8"), 8, "https://images.pexels.com/photos/3550482/pexels-photo-3550482.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 38));
            productRepository.save(new Product(categories.get("computer").getId(), "华硕 ROG 枪神8", "华硕 ROG 枪神8", "Brand", new BigDecimal("14171.9"), 36, "https://images.pexels.com/photos/12877878/pexels-photo-12877878.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 40));
            productRepository.save(new Product(categories.get("computer").getId(), "Dell U2724D", "Dell U2724D", "Brand", new BigDecimal("2685.5"), 30, "https://images.pexels.com/photos/9539073/pexels-photo-9539073.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 263));
            productRepository.save(new Product(categories.get("computer").getId(), "LG 27GR95UM", "LG 27GR95UM", "Brand", new BigDecimal("2543"), 26, "https://images.pexels.com/photos/31726554/pexels-photo-31726554.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 111));
            productRepository.save(new Product(categories.get("computer").getId(), "Logitech MX Master 3S", "Logitech MX Master 3S", "Brand", new BigDecimal("10016"), 87, "https://images.pexels.com/photos/7006949/pexels-photo-7006949.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 151));
            productRepository.save(new Product(categories.get("computer").getId(), "Keychron Q1 Pro", "Keychron Q1 Pro", "Brand", new BigDecimal("10507.5"), 54, "https://images.pexels.com/photos/28534977/pexels-photo-28534977.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 177));
            productRepository.save(new Product(categories.get("computer").getId(), "Cherry MX Board 3.0S", "Cherry MX Board 3.0S", "Brand", new BigDecimal("4928.9"), 132, "https://img0.baidu.com/it/u=1303925179,2827987352&fm=253&fmt=auto&app=138&f=JPEG?w=664&h=500", ProductStatus.ON_SALE, 135));
            productRepository.save(new Product(categories.get("computer").getId(), "Blue Yeti X USB 麦克风", "Blue Yeti X USB 麦克风", "Brand", new BigDecimal("14301.8"), 37, "https://img1.baidu.com/it/u=1489136031,2611933822&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 286));
            productRepository.save(new Product(categories.get("computer").getId(), "WD My Book 12TB", "WD My Book 12TB", "Brand", new BigDecimal("14991"), 116, "https://img0.baidu.com/it/u=3745468111,1581294090&fm=253&fmt=auto&app=120&f=JPEG?w=750&h=500", ProductStatus.ON_SALE, 414));
            productRepository.save(new Product(categories.get("computer").getId(), "TP-Link AXE5400", "TP-Link AXE5400", "Brand", new BigDecimal("2966.5"), 140, "https://img2.baidu.com/it/u=2233837722,3991116661&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 453));
            productRepository.save(new Product(categories.get("computer").getId(), "明基 ScreenBar Halo", "明基 ScreenBar Halo", "Brand", new BigDecimal("8240.9"), 139, "https://img2.baidu.com/it/u=1184447213,3534838191&fm=253&fmt=auto&app=120&f=PNG?w=500&h=500", ProductStatus.ON_SALE, 205));
            productRepository.save(new Product(categories.get("computer").getId(), "爱格升 LX 显示器支架", "爱格升 LX 显示器支架", "Brand", new BigDecimal("11830"), 168, "https://images.pexels.com/photos/326512/pexels-photo-326512.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 288));
            productRepository.save(new Product(categories.get("computer").getId(), "Herman Miller Aeron", "Herman Miller Aeron", "Brand", new BigDecimal("14956.9"), 20, "https://img2.baidu.com/it/u=3120327837,2995872738&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500", ProductStatus.ON_SALE, 39));
            productRepository.save(new Product(categories.get("computer").getId(), "网易严选 人体工学椅", "网易严选 人体工学椅", "Brand", new BigDecimal("7472"), 23, "https://images.pexels.com/photos/31726674/pexels-photo-31726674.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 136));
            productRepository.save(new Product(categories.get("computer").getId(), "Wacom Intuos Pro M", "Wacom Intuos Pro M", "Brand", new BigDecimal("7162"), 51, "https://images.pexels.com/photos/301792/pexels-photo-301792.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 187));
            productRepository.save(new Product(categories.get("computer").getId(), "APC Back-UPS 650", "APC Back-UPS 650", "Brand", new BigDecimal("1866"), 181, "https://img0.baidu.com/it/u=1411786639,1447477904&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 629));
            productRepository.save(new Product(categories.get("computer").getId(), "绿联 雷电4 扩展坞", "绿联 雷电4 扩展坞", "Brand", new BigDecimal("10540"), 88, "https://img2.baidu.com/it/u=3171352984,2912295965&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500", ProductStatus.ON_SALE, 223));
            productRepository.save(new Product(categories.get("computer").getId(), "微软 Surface Pro 10", "微软 Surface Pro 10", "Brand", new BigDecimal("4410.9"), 12, "https://images.pexels.com/photos/1181207/pexels-photo-1181207.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 146));
            // 家居生活 (33件)
            productRepository.save(new Product(categories.get("home").getId(), "大疆 Osmo Mobile 6", "大疆 Osmo Mobile 6", "Brand", new BigDecimal("4457.9"), 38, "https://img0.baidu.com/it/u=1517835298,4291334164&fm=253&fmt=auto&app=138&f=PNG?w=500&h=500", ProductStatus.ON_SALE, 96));
            productRepository.save(new Product(categories.get("home").getId(), "雷蛇 DeathAdder V3 Pro", "雷蛇 DeathAdder V3 Pro", "Brand", new BigDecimal("997.8"), 110, "https://img2.baidu.com/it/u=1040595650,4254275842&fm=253&fmt=auto&app=120&f=JPEG?w=750&h=500", ProductStatus.ON_SALE, 110));
            productRepository.save(new Product(categories.get("home").getId(), "群晖 DS224+", "群晖 DS224+", "Brand", new BigDecimal("153.8"), 15, "https://img1.baidu.com/it/u=3785763056,3335499213&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333", ProductStatus.ON_SALE, 58));
            productRepository.save(new Product(categories.get("home").getId(), "罗技 StreamCam", "罗技 StreamCam", "Brand", new BigDecimal("2118.5"), 114, "https://img1.baidu.com/it/u=4156895928,683121194&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500", ProductStatus.ON_SALE, 170));
            productRepository.save(new Product(categories.get("home").getId(), "戴森 V15 Detect", "戴森 V15 Detect", "Brand", new BigDecimal("3893.5"), 16, "https://img1.baidu.com/it/u=1483448799,1375287622&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=774", ProductStatus.ON_SALE, 190));
            productRepository.save(new Product(categories.get("home").getId(), "戴森 Airwrap HS05", "戴森 Airwrap HS05", "Brand", new BigDecimal("3185.8"), 29, "https://t13.baidu.com/it/u=1689246111,1850979139&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 56));
            productRepository.save(new Product(categories.get("home").getId(), "飞利浦 空气炸锅 HD9651", "飞利浦 空气炸锅 HD9651", "Brand", new BigDecimal("579.5"), 87, "https://images.pexels.com/photos/35285814/pexels-photo-35285814.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 141));
            productRepository.save(new Product(categories.get("home").getId(), "德龙 ECAM23.460 全自动咖啡机", "德龙 ECAM23.460 全自动咖啡机", "Brand", new BigDecimal("3517.8"), 43, "https://images.pexels.com/photos/6612594/pexels-photo-6612594.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 270));
            productRepository.save(new Product(categories.get("home").getId(), "松下 SR-HBC184 电饭煲", "松下 SR-HBC184 电饭煲", "Brand", new BigDecimal("1369.9"), 34, "https://img2.baidu.com/it/u=309271609,3353860499&fm=253&fmt=auto&app=138&f=JPEG?w=625&h=475", ProductStatus.ON_SALE, 142));
            productRepository.save(new Product(categories.get("home").getId(), "九阳 DJ13B-D08EC 豆浆机", "九阳 DJ13B-D08EC 豆浆机", "Brand", new BigDecimal("957"), 59, "https://img1.baidu.com/it/u=231981053,927130918&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 91));
            productRepository.save(new Product(categories.get("home").getId(), "戴森 Pure Cool 空气净化风扇", "戴森 Pure Cool 空气净化风扇", "Brand", new BigDecimal("444.9"), 28, "https://img2.baidu.com/it/u=574379575,3941654527&fm=253&fmt=auto&app=138&f=JPEG?w=778&h=500", ProductStatus.ON_SALE, 277));
            productRepository.save(new Product(categories.get("home").getId(), "Aqara 智能窗帘电机 C3", "Aqara 智能窗帘电机 C3", "Brand", new BigDecimal("4101"), 97, "https://img2.baidu.com/it/u=2005056093,4174509323&fm=253&fmt=auto?w=800&h=800", ProductStatus.ON_SALE, 390));
            productRepository.save(new Product(categories.get("home").getId(), "Yeelight 智能吸顶灯 Pro", "Yeelight 智能吸顶灯 Pro", "Brand", new BigDecimal("1192.5"), 50, "https://img1.baidu.com/it/u=1979791940,4138154734&fm=253&fmt=auto&app=138&f=JPEG?w=595&h=500", ProductStatus.ON_SALE, 82));
            productRepository.save(new Product(categories.get("home").getId(), "德业 DYD-T22A3 除湿机", "德业 DYD-T22A3 除湿机", "Brand", new BigDecimal("3024"), 114, "https://img0.baidu.com/it/u=188974569,1438514826&fm=253&fmt=auto&app=138&f=PNG?w=500&h=500", ProductStatus.ON_SALE, 323));
            productRepository.save(new Product(categories.get("home").getId(), "MUJI 超声波香薰机", "MUJI 超声波香薰机", "Brand", new BigDecimal("4839.5"), 173, "https://img0.baidu.com/it/u=194095514,4159842746&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=584", ProductStatus.ON_SALE, 381));
            productRepository.save(new Product(categories.get("home").getId(), "象印 SM-SG48 保温杯", "象印 SM-SG48 保温杯", "Brand", new BigDecimal("4847.5"), 19, "https://img1.baidu.com/it/u=2874339387,1517446486&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 234));
            productRepository.save(new Product(categories.get("home").getId(), "水星家纺 95鹅绒被", "水星家纺 95鹅绒被", "Brand", new BigDecimal("2211.9"), 81, "https://t14.baidu.com/it/u=855922978,2356646813&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 647));
            productRepository.save(new Product(categories.get("home").getId(), "天堂 晴雨伞 三折", "天堂 晴雨伞 三折", "Brand", new BigDecimal("4657.5"), 93, "https://img0.baidu.com/it/u=167956290,3650208405&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 421));
            productRepository.save(new Product(categories.get("home").getId(), "3M 净水器 SDW8000", "3M 净水器 SDW8000", "Brand", new BigDecimal("2079.9"), 89, "https://t14.baidu.com/it/u=745198561,2055567374&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 106));
            productRepository.save(new Product(categories.get("home").getId(), "Bruno 多功能料理锅", "Bruno 多功能料理锅", "Brand", new BigDecimal("2228"), 88, "https://img1.baidu.com/it/u=2187933602,2104651768&fm=253&fmt=auto&app=138&f=JPEG?w=525&h=500", ProductStatus.ON_SALE, 521));
            productRepository.save(new Product(categories.get("home").getId(), "美的 M1-L213C 微波炉", "美的 M1-L213C 微波炉", "Brand", new BigDecimal("3817"), 35, "https://img0.baidu.com/it/u=345474562,3394396815&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667", ProductStatus.ON_SALE, 165));
            productRepository.save(new Product(categories.get("home").getId(), "安踏 C37 5.0 休闲鞋", "安踏 C37 5.0 休闲鞋", "Brand", new BigDecimal("4148.5"), 147, "https://img2.baidu.com/it/u=4265225706,1640541864&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 80));
            productRepository.save(new Product(categories.get("home").getId(), "Stanley 户外保温箱 15L", "Stanley 户外保温箱 15L", "Brand", new BigDecimal("2636"), 99, "https://images.pexels.com/photos/14974681/pexels-photo-14974681.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 478));
            productRepository.save(new Product(categories.get("home").getId(), "佳明 Forerunner 265", "佳明 Forerunner 265", "Brand", new BigDecimal("2382.9"), 77, "https://img1.baidu.com/it/u=3514582108,2762469287&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1067", ProductStatus.ON_SALE, 470));
            productRepository.save(new Product(categories.get("home").getId(), "崔克 Domane AL 4", "崔克 Domane AL 4", "Brand", new BigDecimal("1787.9"), 40, "https://img1.baidu.com/it/u=2945818137,1082357257&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1067", ProductStatus.ON_SALE, 65));
            productRepository.save(new Product(categories.get("home").getId(), "Charlotte Tilbury 枕边话", "Charlotte Tilbury 枕边话", "Brand", new BigDecimal("2252"), 220, "https://t15.baidu.com/it/u=2810088560,3233639926&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 455));
            productRepository.save(new Product(categories.get("home").getId(), "潘海利根 兽首狐狸 75ml", "潘海利根 兽首狐狸 75ml", "Brand", new BigDecimal("2643.5"), 52, "https://img1.baidu.com/it/u=92314570,2067180411&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 339));
            productRepository.save(new Product(categories.get("home").getId(), "Marvis 牙膏套装 7支", "Marvis 牙膏套装 7支", "Brand", new BigDecimal("321.9"), 192, "https://images.pexels.com/photos/3735649/pexels-photo-3735649.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 402));
            productRepository.save(new Product(categories.get("home").getId(), "illy 深度烘焙咖啡粉 250g", "illy 深度烘焙咖啡粉 250g", "Brand", new BigDecimal("517"), 176, "https://images.pexels.com/photos/27528587/pexels-photo-27528587.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 153));
            productRepository.save(new Product(categories.get("home").getId(), "瑞幸 冻干咖啡粉 12颗", "瑞幸 冻干咖啡粉 12颗", "Brand", new BigDecimal("2715.9"), 250, "https://img0.baidu.com/it/u=191208523,2208279420&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500", ProductStatus.ON_SALE, 1190));
            productRepository.save(new Product(categories.get("home").getId(), "永璞 闪萃咖啡液 14杯", "永璞 闪萃咖啡液 14杯", "Brand", new BigDecimal("4621"), 224, "https://t14.baidu.com/it/u=133397750,3904153112&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 498));
            productRepository.save(new Product(categories.get("home").getId(), "安佳 全脂奶粉 1kg", "安佳 全脂奶粉 1kg", "Brand", new BigDecimal("2270.9"), 196, "https://t14.baidu.com/it/u=856171722,2999156335&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 282));
            productRepository.save(new Product(categories.get("home").getId(), "奔富 Bin 389 干红 750ml", "奔富 Bin 389 干红 750ml", "Brand", new BigDecimal("3845.5"), 22, "https://img1.baidu.com/it/u=3331296355,1125988652&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 214));
            // 运动户外 (21件)
            productRepository.save(new Product(categories.get("sport").getId(), "Nike Air Zoom Pegasus 40", "Nike Air Zoom Pegasus 40", "Brand", new BigDecimal("2054"), 100, "https://images.pexels.com/photos/2043476/pexels-photo-2043476.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 85));
            productRepository.save(new Product(categories.get("sport").getId(), "Adidas Ultraboost Light", "Adidas Ultraboost Light", "Brand", new BigDecimal("6186.9"), 89, "https://images.pexels.com/photos/11883282/pexels-photo-11883282.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 350));
            productRepository.save(new Product(categories.get("sport").getId(), "李宁 超轻21 跑鞋", "李宁 超轻21 跑鞋", "Brand", new BigDecimal("5250"), 158, "https://images.pexels.com/photos/260044/pexels-photo-260044.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 540));
            productRepository.save(new Product(categories.get("sport").getId(), "Lululemon Align 瑜伽裤", "Lululemon Align 瑜伽裤", "Brand", new BigDecimal("633.8"), 84, "https://img2.baidu.com/it/u=3594144515,837873919&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667", ProductStatus.ON_SALE, 385));
            productRepository.save(new Product(categories.get("sport").getId(), "Keep 瑜伽垫 TPE", "Keep 瑜伽垫 TPE", "Brand", new BigDecimal("5648.8"), 50, "https://images.pexels.com/photos/6246682/pexels-photo-6246682.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 485));
            productRepository.save(new Product(categories.get("sport").getId(), "Nike Pro 运动内衣", "Nike Pro 运动内衣", "Brand", new BigDecimal("2236.9"), 22, "https://t13.baidu.com/it/u=112317752,569248824&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 79));
            productRepository.save(new Product(categories.get("sport").getId(), "迪卡侬 MH500 登山包", "迪卡侬 MH500 登山包", "Brand", new BigDecimal("1765.8"), 13, "https://images.pexels.com/photos/1178525/pexels-photo-1178525.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 27));
            productRepository.save(new Product(categories.get("sport").getId(), "The North Face 羽绒服 96Nuptse", "The North Face 羽绒服 96Nuptse", "Brand", new BigDecimal("3081"), 23, "https://img2.baidu.com/it/u=1833124489,1439708820&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667", ProductStatus.ON_SALE, 170));
            productRepository.save(new Product(categories.get("sport").getId(), "骆驼 露营帐篷 3-4人", "骆驼 露营帐篷 3-4人", "Brand", new BigDecimal("3037"), 40, "https://images.pexels.com/photos/17192955/pexels-photo-17192955.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 132));
            productRepository.save(new Product(categories.get("sport").getId(), "YETI Rambler 水壶 769ml", "YETI Rambler 水壶 769ml", "Brand", new BigDecimal("5256.9"), 168, "https://images.pexels.com/photos/8266854/pexels-photo-8266854.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 408));
            productRepository.save(new Product(categories.get("sport").getId(), "Peloton Bike+ 健身车", "Peloton Bike+ 健身车", "Brand", new BigDecimal("5137.5"), 49, "https://images.pexels.com/photos/24244151/pexels-photo-24244151.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 107));
            productRepository.save(new Product(categories.get("sport").getId(), "Keep 智能跳绳", "Keep 智能跳绳", "Brand", new BigDecimal("876.5"), 128, "https://t13.baidu.com/it/u=842414330,3255769619&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 771));
            productRepository.save(new Product(categories.get("sport").getId(), "哑铃套装 20kg 可调节", "哑铃套装 20kg 可调节", "Brand", new BigDecimal("1956.9"), 48, "https://images.pexels.com/photos/7743320/pexels-photo-7743320.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 215));
            productRepository.save(new Product(categories.get("sport").getId(), "Nike 瑜伽训练垫 5mm", "Nike 瑜伽训练垫 5mm", "Brand", new BigDecimal("2419"), 143, "https://img0.baidu.com/it/u=2676652242,2411546301&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=719", ProductStatus.ON_SALE, 409));
            productRepository.save(new Product(categories.get("sport").getId(), "Garmin Fenix 7X Pro", "Garmin Fenix 7X Pro", "Brand", new BigDecimal("1915.9"), 21, "https://images.pexels.com/photos/3999644/pexels-photo-3999644.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 142));
            productRepository.save(new Product(categories.get("sport").getId(), "Shimano RC7 骑行锁鞋", "Shimano RC7 骑行锁鞋", "Brand", new BigDecimal("618"), 114, "https://images.pexels.com/photos/5807638/pexels-photo-5807638.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 730));
            productRepository.save(new Product(categories.get("sport").getId(), "Speedo Fastskin 泳镜", "Speedo Fastskin 泳镜", "Brand", new BigDecimal("1624.9"), 140, "https://images.pexels.com/photos/28939350/pexels-photo-28939350.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 612));
            productRepository.save(new Product(categories.get("sport").getId(), "Arena 游泳训练脚蹼", "Arena 游泳训练脚蹼", "Brand", new BigDecimal("3011.8"), 233, "https://t13.baidu.com/it/u=2101141292,2667305142&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 289));
            productRepository.save(new Product(categories.get("sport").getId(), "Wilson NBA 篮球", "Wilson NBA 篮球", "Brand", new BigDecimal("5198.8"), 146, "https://images.pexels.com/photos/13179883/pexels-photo-13179883.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 284));
            productRepository.save(new Product(categories.get("sport").getId(), "Yonex 天斧 AX100ZZ", "Yonex 天斧 AX100ZZ", "Brand", new BigDecimal("5199.9"), 71, "https://images.pexels.com/photos/11312132/pexels-photo-11312132.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 473));
            productRepository.save(new Product(categories.get("sport").getId(), "Garmin Edge 540 码表", "Garmin Edge 540 码表", "Brand", new BigDecimal("694.5"), 25, "https://img1.baidu.com/it/u=861522703,1135581721&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667", ProductStatus.ON_SALE, 71));
            // 美妆个护 (17件)
            productRepository.save(new Product(categories.get("beauty").getId(), "兰蔻 极光水 150ml", "兰蔻 极光水 150ml", "Brand", new BigDecimal("3796.5"), 50, "https://img2.baidu.com/it/u=2500045959,1603506790&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1039", ProductStatus.ON_SALE, 316));
            productRepository.save(new Product(categories.get("beauty").getId(), "SK-II 神仙水 230ml", "SK-II 神仙水 230ml", "Brand", new BigDecimal("2215"), 98, "https://img0.baidu.com/it/u=3085469977,554236281&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1067", ProductStatus.ON_SALE, 453));
            productRepository.save(new Product(categories.get("beauty").getId(), "雅诗兰黛 小棕瓶精华 50ml", "雅诗兰黛 小棕瓶精华 50ml", "Brand", new BigDecimal("3007.5"), 107, "https://images.pexels.com/photos/7321654/pexels-photo-7321654.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 405));
            productRepository.save(new Product(categories.get("beauty").getId(), "海蓝之谜 面霜 60ml", "海蓝之谜 面霜 60ml", "Brand", new BigDecimal("3324"), 58, "https://images.pexels.com/photos/6635929/pexels-photo-6635929.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 205));
            productRepository.save(new Product(categories.get("beauty").getId(), "Olay 小白瓶 40ml", "Olay 小白瓶 40ml", "Brand", new BigDecimal("2491"), 113, "https://t14.baidu.com/it/u=2678910981,1556072268&fm=224&app=112&f=JPEG?w=500&h=499", ProductStatus.ON_SALE, 423));
            productRepository.save(new Product(categories.get("beauty").getId(), "珀莱雅 双抗精华 30ml", "珀莱雅 双抗精华 30ml", "Brand", new BigDecimal("3490"), 101, "https://img0.baidu.com/it/u=3477624241,339296795&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=889", ProductStatus.ON_SALE, 529));
            productRepository.save(new Product(categories.get("beauty").getId(), "迪奥 口红 999 哑光", "迪奥 口红 999 哑光", "Brand", new BigDecimal("415"), 176, "https://images.pexels.com/photos/6648498/pexels-photo-6648498.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 800));
            productRepository.save(new Product(categories.get("beauty").getId(), "Tom Ford 黑金唇膏 16", "Tom Ford 黑金唇膏 16", "Brand", new BigDecimal("398.5"), 85, "https://img2.baidu.com/it/u=121355351,2212544591&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=774", ProductStatus.ON_SALE, 539));
            productRepository.save(new Product(categories.get("beauty").getId(), "YSL 小金条 #21", "YSL 小金条 #21", "Brand", new BigDecimal("1397"), 67, "https://img1.baidu.com/it/u=292920495,3787263984&fm=253&fmt=auto&app=138&f=JPEG?w=924&h=800", ProductStatus.ON_SALE, 120));
            productRepository.save(new Product(categories.get("beauty").getId(), "MAC 生姜高光 DoubleGleam", "MAC 生姜高光 DoubleGleam", "Brand", new BigDecimal("900.9"), 71, "https://img0.baidu.com/it/u=1455105527,2632334290&fm=253&fmt=auto&app=138&f=JPEG?w=514&h=500", ProductStatus.ON_SALE, 383));
            productRepository.save(new Product(categories.get("beauty").getId(), "Jo Malone 蓝风铃 30ml", "Jo Malone 蓝风铃 30ml", "Brand", new BigDecimal("1157"), 54, "https://img1.baidu.com/it/u=4261862670,2492828225&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=613", ProductStatus.ON_SALE, 440));
            productRepository.save(new Product(categories.get("beauty").getId(), "Diptyque 檀道 75ml", "Diptyque 檀道 75ml", "Brand", new BigDecimal("1750"), 109, "https://images.pexels.com/photos/2866796/pexels-photo-2866796.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 265));
            productRepository.save(new Product(categories.get("beauty").getId(), "Aesop 赋活芳香护手霜 75ml", "Aesop 赋活芳香护手霜 75ml", "Brand", new BigDecimal("672.9"), 178, "https://images.pexels.com/photos/17788297/pexels-photo-17788297.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 383));
            productRepository.save(new Product(categories.get("beauty").getId(), "卡诗 白金赋活洗发水 250ml", "卡诗 白金赋活洗发水 250ml", "Brand", new BigDecimal("3127.9"), 159, "https://t14.baidu.com/it/u=1654641394,3745738925&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 417));
            productRepository.save(new Product(categories.get("beauty").getId(), "戴森 Supersonic 吹风机", "戴森 Supersonic 吹风机", "Brand", new BigDecimal("3579"), 123, "https://img1.baidu.com/it/u=23607614,1665746621&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=654", ProductStatus.ON_SALE, 315));
            productRepository.save(new Product(categories.get("beauty").getId(), "CeraVe 保湿洁面乳 473ml", "CeraVe 保湿洁面乳 473ml", "Brand", new BigDecimal("1158.9"), 167, "https://images.pexels.com/photos/14107358/pexels-photo-14107358.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 421));
            productRepository.save(new Product(categories.get("beauty").getId(), "修丽可 CF 抗氧化精华 30ml", "修丽可 CF 抗氧化精华 30ml", "Brand", new BigDecimal("1858.8"), 21, "https://t15.baidu.com/it/u=2862061342,1359681833&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 131));
            // 食品饮料 (15件)
            productRepository.save(new Product(categories.get("food").getId(), "三只松鼠 每日坚果 750g", "三只松鼠 每日坚果 750g", "Brand", new BigDecimal("1727"), 152, "https://images.pexels.com/photos/5507656/pexels-photo-5507656.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 489));
            productRepository.save(new Product(categories.get("food").getId(), "良品铺子 猪肉脯 500g", "良品铺子 猪肉脯 500g", "Brand", new BigDecimal("2881.5"), 179, "https://images.pexels.com/photos/5237009/pexels-photo-5237009.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 740));
            productRepository.save(new Product(categories.get("food").getId(), "瑞士莲 软心巧克力 600g", "瑞士莲 软心巧克力 600g", "Brand", new BigDecimal("2941.8"), 316, "https://images.pexels.com/photos/6167328/pexels-photo-6167328.jpeg?w=400&h=300&fit=crop", ProductStatus.ON_SALE, 996));
            productRepository.save(new Product(categories.get("food").getId(), "认养一头牛 纯牛奶 250ml×24盒", "认养一头牛 纯牛奶 250ml×24盒", "Brand", new BigDecimal("2307"), 219, "https://t15.baidu.com/it/u=2261298056,2831031781&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 701));
            productRepository.save(new Product(categories.get("food").getId(), "北海牧场 酸奶 200g×12杯", "北海牧场 酸奶 200g×12杯", "Brand", new BigDecimal("2474"), 235, "https://img0.baidu.com/it/u=3735118658,3701049556&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 751));
            productRepository.save(new Product(categories.get("food").getId(), "桂格 即食燕麦片 1.5kg", "桂格 即食燕麦片 1.5kg", "Brand", new BigDecimal("961.5"), 246, "https://t15.baidu.com/it/u=3884123748,36247541&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 592));
            productRepository.save(new Product(categories.get("food").getId(), "汤臣倍健 蛋白粉 450g", "汤臣倍健 蛋白粉 450g", "Brand", new BigDecimal("392"), 94, "https://t13.baidu.com/it/u=182116611,1399511843&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 288));
            productRepository.save(new Product(categories.get("food").getId(), "东方树叶 茉莉花茶 500ml×15", "东方树叶 茉莉花茶 500ml×15", "Brand", new BigDecimal("139"), 105, "https://img2.baidu.com/it/u=4238658415,1843742652&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1220", ProductStatus.ON_SALE, 435));
            productRepository.save(new Product(categories.get("food").getId(), "农夫山泉 矿泉水 550ml×24", "农夫山泉 矿泉水 550ml×24", "Brand", new BigDecimal("2427.5"), 214, "https://t14.baidu.com/it/u=2904280698,2988415572&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 177));
            productRepository.save(new Product(categories.get("food").getId(), "青岛啤酒 经典1903 500ml×12", "青岛啤酒 经典1903 500ml×12", "Brand", new BigDecimal("2242"), 211, "https://img1.baidu.com/it/u=3023766857,170739254&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 776));
            productRepository.save(new Product(categories.get("food").getId(), "锐澳 微醺鸡尾酒 330ml×8", "锐澳 微醺鸡尾酒 330ml×8", "Brand", new BigDecimal("428.8"), 34, "https://img1.baidu.com/it/u=3046731875,2649461416&fm=253&fmt=auto&app=138&f=JPEG?w=535&h=500", ProductStatus.ON_SALE, 95));
            productRepository.save(new Product(categories.get("food").getId(), "茅台 飞天53度 500ml", "茅台 飞天53度 500ml", "Brand", new BigDecimal("923"), 20, "https://img2.baidu.com/it/u=3503855494,4060913369&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=674", ProductStatus.ON_SALE, 191));
            productRepository.save(new Product(categories.get("food").getId(), "奈雪的茶 茉莉初雪 茶包 15袋", "奈雪的茶 茉莉初雪 茶包 15袋", "Brand", new BigDecimal("1135"), 157, "https://t13.baidu.com/it/u=3837628902,3119685359&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 404));
            productRepository.save(new Product(categories.get("food").getId(), "小猪呵呵 午餐肉 340g", "小猪呵呵 午餐肉 340g", "Brand", new BigDecimal("111.9"), 214, "https://t14.baidu.com/it/u=2402713266,3628962914&fm=224&app=112&f=JPEG?w=500&h=500", ProductStatus.ON_SALE, 585));
            productRepository.save(new Product(categories.get("food").getId(), "沃隆 每日坚果 礼盒装 1kg", "沃隆 每日坚果 礼盒装 1kg", "Brand", new BigDecimal("2628"), 112, "https://img2.baidu.com/it/u=2219028154,3222013428&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1067", ProductStatus.ON_SALE, 863));
        }
        List<Product> products = productRepository.findAll();
        Map<String, Product> prodMap = new LinkedHashMap<>();
        for (Product p : products) prodMap.put(p.getName(), p);
        return prodMap;
    }

    private void seedBehaviors(Map<String, User> users, Map<String, Product> products) {
        if (userBehaviorRepository.count() > 0) return;

        List<Product> phone=products.values().stream().filter(p->p.getCategoryId()==1).toList();
        List<Product> comp=products.values().stream().filter(p->p.getCategoryId()==2).toList();
        List<Product> home=products.values().stream().filter(p->p.getCategoryId()==3).toList();
        List<Product> sport=products.values().stream().filter(p->p.getCategoryId()==4).toList();
        List<Product> beauty=products.values().stream().filter(p->p.getCategoryId()==5).toList();
        List<Product> food=products.values().stream().filter(p->p.getCategoryId()==6).toList();

        var rng = new java.util.Random(42);

        // alice: 手机数码 70%，电脑15%，运动15%
        behave(users.get("alice"), 20, phone, comp, sport, rng);
        // bob: 电脑办公 70%，手机15%，家居15%
        behave(users.get("bob"), 20, comp, phone, home, rng);
        // carol: 家居生活 70%，运动15%，食品15%
        behave(users.get("carol"), 20, home, sport, food, rng);
        // dave: 运动户外 70%，手机15%，家居15%
        behave(users.get("dave"), 20, sport, phone, home, rng);
        // eve: 手机60%+运动40%（连接 alice ↔ dave）
        behaveCross(users.get("eve"), 20, phone, sport, rng);
        // frank: 电脑60%+家居40%（连接 bob ↔ carol）
        behaveCross(users.get("frank"), 20, comp, home, rng);
    }

    private void behave(User user, int count, List<Product> main, List<Product> alt1,
                        List<Product> alt2, java.util.Random rng) {
        for (int i = 0; i < count; i++) {
            double roll = rng.nextDouble();
            Product p;
            if (roll < 0.85) p = main.get(rng.nextInt(main.size()));
            else if (roll < 0.95) p = alt1.get(rng.nextInt(alt1.size()));
            else p = alt2.get(rng.nextInt(alt2.size()));

            behaviorService.recordView(user.getId(), p.getId());
            if (rng.nextDouble() < 0.4) behaviorService.recordCart(user.getId(), p.getId());
            if (rng.nextDouble() < 0.25) behaviorService.recordOrder(user.getId(), p.getId());
        }
    }

    private void behaveCross(User user, int count, List<Product> catA, List<Product> catB,
                             java.util.Random rng) {
        for (int i = 0; i < count; i++) {
            Product p = rng.nextBoolean() ? catA.get(rng.nextInt(catA.size()))
                    : catB.get(rng.nextInt(catB.size()));
            behaviorService.recordView(user.getId(), p.getId());
            if (rng.nextDouble() < 0.4) behaviorService.recordCart(user.getId(), p.getId());
            if (rng.nextDouble() < 0.25) behaviorService.recordOrder(user.getId(), p.getId());
        }
    }

    private Product product(Category category, String slug, String name, String description, String brand,
                            String price, int stock, int salesCount) {
        String img = IMG.get(name);
        if (img == null) img = "https://picsum.photos/seed/" + slug + "/640/480";
        return new Product(category.getId(), name, description, brand, new BigDecimal(price), stock,
                img,
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
