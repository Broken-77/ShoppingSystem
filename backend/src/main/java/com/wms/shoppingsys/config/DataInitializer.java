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
        IMG.put("Nova 12 Ultra", "https://img2.baidu.com/it/u=1604138311,3450159460&fm=253&app=138&f=JPEG?w=800&h=1067");
        IMG.put("AirPods Pro 2", "https://images.pexels.com/photos/3921845/pexels-photo-3921845.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 FreeBuds Pro 3", "https://images.pexels.com/photos/14741306/pexels-photo-14741306.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 Buds 4 Pro", "https://images.pexels.com/photos/10902946/pexels-photo-10902946.jpeg?w=400&h=300&fit=crop");
        IMG.put("Apple Watch Ultra 2", "https://images.pexels.com/photos/5083218/pexels-photo-5083218.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 Watch GT 4", "https://img0.baidu.com/it/u=2431263106,894007510&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1410");
        IMG.put("小米手环 8 Pro", "https://img0.baidu.com/it/u=3372477375,240718339&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=653");
        IMG.put("iPad Air M2", "https://images.pexels.com/photos/10535365/pexels-photo-10535365.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 MatePad Pro 13.2", "https://images.pexels.com/photos/33118984/pexels-photo-33118984.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米平板 6S Pro", "https://images.pexels.com/photos/6913311/pexels-photo-6913311.jpeg?w=400&h=300&fit=crop");
        IMG.put("ANKER 65W 氮化镓充电器", "https://images.pexels.com/photos/1028674/pexels-photo-1028674.jpeg?w=400&h=300&fit=crop");
        IMG.put("闪迪 256GB TF 存储卡", "https://img2.baidu.com/it/u=4160309347,225796644&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=840");
        IMG.put("绿联 手机支架 桌面", "https://img0.baidu.com/it/u=514960464,4207606464&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=888");
        IMG.put("大疆 Osmo Mobile 6", "https://img0.baidu.com/it/u=1517835298,4291334164&fm=253&fmt=auto&app=138&f=PNG?w=500&h=500");
        IMG.put("Sony WH-1000XM5", "https://images.pexels.com/photos/37668074/pexels-photo-37668074.jpeg?w=400&h=300&fit=crop");
        IMG.put("Bose QC Ultra", "https://images.pexels.com/photos/29617989/pexels-photo-29617989.jpeg?w=400&h=300&fit=crop");
        IMG.put("JBL Flip 6", "https://images.pexels.com/photos/20285556/pexels-photo-20285556.jpeg?w=400&h=300&fit=crop");
        IMG.put("Kindle Paperwhite 5", "https://img0.baidu.com/it/u=4107566279,1156054415&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("GoPro Hero 12 Black", "https://images.pexels.com/photos/8799699/pexels-photo-8799699.jpeg?w=400&h=300&fit=crop");
        IMG.put("Insta360 X4", "https://images.pexels.com/photos/6630001/pexels-photo-6630001.jpeg?w=400&h=300&fit=crop");
        IMG.put("MacBook Pro 14 M3", "https://images.pexels.com/photos/10655906/pexels-photo-10655906.jpeg?w=400&h=300&fit=crop");
        IMG.put("ThinkPad X1 Carbon Gen 12", "https://images.pexels.com/photos/3550482/pexels-photo-3550482.jpeg?w=400&h=300&fit=crop");
        IMG.put("华硕 ROG 枪神8", "https://images.pexels.com/photos/12877878/pexels-photo-12877878.jpeg?w=400&h=300&fit=crop");
        IMG.put("华为 MateBook X Pro 2024", "https://img2.baidu.com/it/u=3832484179,1808499694&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667");
        IMG.put("小米 Book Pro 16", "https://img1.baidu.com/it/u=1237521389,3022206281&fm=253&fmt=auto&app=120&f=JPEG?w=887&h=800");
        IMG.put("Dell U2724D", "https://images.pexels.com/photos/9539073/pexels-photo-9539073.jpeg?w=400&h=300&fit=crop");
        IMG.put("LG 27GR95UM", "https://images.pexels.com/photos/31726554/pexels-photo-31726554.jpeg?w=400&h=300&fit=crop");
        IMG.put("Samsung ViewFinity S9", "https://images.pexels.com/photos/214488/pexels-photo-214488.jpeg?w=400&h=300&fit=crop");
        IMG.put("Logitech MX Master 3S", "https://images.pexels.com/photos/7006949/pexels-photo-7006949.jpeg?w=400&h=300&fit=crop");
        IMG.put("雷蛇 DeathAdder V3 Pro", "https://img2.baidu.com/it/u=1040595650,4254275842&fm=253&fmt=auto&app=120&f=JPEG?w=750&h=500");
        IMG.put("Keychron Q1 Pro", "https://images.pexels.com/photos/28534977/pexels-photo-28534977.jpeg?w=400&h=300&fit=crop");
        IMG.put("Cherry MX Board 3.0S", "https://img0.baidu.com/it/u=1303925179,2827987352&fm=253&fmt=auto&app=138&f=JPEG?w=664&h=500");
        IMG.put("罗技 G Pro X 耳机", "https://images.pexels.com/photos/210927/pexels-photo-210927.jpeg?w=400&h=300&fit=crop");
        IMG.put("Blue Yeti X USB 麦克风", "https://img1.baidu.com/it/u=1489136031,2611933822&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("Samsung T7 Shield 2TB", "https://img2.baidu.com/it/u=1699828447,2848501117&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=584");
        IMG.put("WD My Book 12TB", "https://img0.baidu.com/it/u=3745468111,1581294090&fm=253&fmt=auto&app=120&f=JPEG?w=750&h=500");
        IMG.put("群晖 DS224+", "https://img1.baidu.com/it/u=3785763056,3335499213&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=333");
        IMG.put("TP-Link AXE5400", "https://img2.baidu.com/it/u=2233837722,3991116661&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("华为 AX6", "https://img0.baidu.com/it/u=2077417276,3289627424&fm=253&fmt=auto?w=640&h=677");
        IMG.put("明基 ScreenBar Halo", "https://img2.baidu.com/it/u=1184447213,3534838191&fm=253&fmt=auto&app=120&f=PNG?w=500&h=500");
        IMG.put("爱格升 LX 显示器支架", "https://images.pexels.com/photos/326512/pexels-photo-326512.jpeg?w=400&h=300&fit=crop");
        IMG.put("Herman Miller Aeron", "https://img2.baidu.com/it/u=3120327837,2995872738&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500");
        IMG.put("网易严选 人体工学椅", "https://images.pexels.com/photos/31726674/pexels-photo-31726674.jpeg?w=400&h=300&fit=crop");
        IMG.put("Wacom Intuos Pro M", "https://images.pexels.com/photos/301792/pexels-photo-301792.jpeg?w=400&h=300&fit=crop");
        IMG.put("罗技 StreamCam", "https://img1.baidu.com/it/u=4156895928,683121194&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=500");
        IMG.put("APC Back-UPS 650", "https://img0.baidu.com/it/u=1411786639,1447477904&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("绿联 雷电4 扩展坞", "https://img2.baidu.com/it/u=3171352984,2912295965&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500");
        IMG.put("微软 Surface Pro 10", "https://images.pexels.com/photos/1181207/pexels-photo-1181207.jpeg?w=400&h=300&fit=crop");
        IMG.put("戴森 V15 Detect", "https://img1.baidu.com/it/u=1483448799,1375287622&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=774");
        IMG.put("小米 扫拖机器人 X20+", "https://img1.baidu.com/it/u=3593545207,3113610742&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500");
        IMG.put("戴森 Airwrap HS05", "https://t13.baidu.com/it/u=1689246111,1850979139&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("飞利浦 空气炸锅 HD9651", "https://images.pexels.com/photos/35285814/pexels-photo-35285814.jpeg?w=400&h=300&fit=crop");
        IMG.put("德龙 ECAM23.460 全自动咖啡机", "https://images.pexels.com/photos/6612594/pexels-photo-6612594.jpeg?w=400&h=300&fit=crop");
        IMG.put("松下 SR-HBC184 电饭煲", "https://img2.baidu.com/it/u=309271609,3353860499&fm=253&fmt=auto&app=138&f=JPEG?w=625&h=475");
        IMG.put("九阳 DJ13B-D08EC 豆浆机", "https://img1.baidu.com/it/u=231981053,927130918&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("Bose SoundLink Max", "https://img1.baidu.com/it/u=3581404649,53606868&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=750");
        IMG.put("戴森 Pure Cool 空气净化风扇", "https://img2.baidu.com/it/u=574379575,3941654527&fm=253&fmt=auto&app=138&f=JPEG?w=778&h=500");
        IMG.put("Aqara 智能窗帘电机 C3", "https://img2.baidu.com/it/u=2005056093,4174509323&fm=253&fmt=auto?w=800&h=800");
        IMG.put("Yeelight 智能吸顶灯 Pro", "https://img1.baidu.com/it/u=1979791940,4138154734&fm=253&fmt=auto&app=138&f=JPEG?w=595&h=500");
        IMG.put("德业 DYD-T22A3 除湿机", "https://img0.baidu.com/it/u=188974569,1438514826&fm=253&fmt=auto&app=138&f=PNG?w=500&h=500");
        IMG.put("小米 空气净化器 4 Pro", "https://img1.baidu.com/it/u=1909335717,36263449&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("MUJI 超声波香薰机", "https://img0.baidu.com/it/u=194095514,4159842746&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=584");
        IMG.put("象印 SM-SG48 保温杯", "https://img1.baidu.com/it/u=2874339387,1517446486&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("水星家纺 95鹅绒被", "https://t14.baidu.com/it/u=855922978,2356646813&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("天堂 晴雨伞 三折", "https://img0.baidu.com/it/u=167956290,3650208405&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500");
        IMG.put("3M 净水器 SDW8000", "https://t14.baidu.com/it/u=745198561,2055567374&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("小米 电热水壶 2", "https://img1.baidu.com/it/u=3932252322,2859413546&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=967");
        IMG.put("Bruno 多功能料理锅", "https://img1.baidu.com/it/u=2187933602,2104651768&fm=253&fmt=auto&app=138&f=JPEG?w=525&h=500");
        IMG.put("美的 M1-L213C 微波炉", "https://img0.baidu.com/it/u=345474562,3394396815&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667");
        IMG.put("Nike Air Zoom Pegasus 40", "https://images.pexels.com/photos/2043476/pexels-photo-2043476.jpeg?w=400&h=300&fit=crop");
        IMG.put("Adidas Ultraboost Light", "https://images.pexels.com/photos/11883282/pexels-photo-11883282.jpeg?w=400&h=300&fit=crop");
        IMG.put("李宁 超轻21 跑鞋", "https://images.pexels.com/photos/260044/pexels-photo-260044.jpeg?w=400&h=300&fit=crop");
        IMG.put("安踏 C37 5.0 休闲鞋", "https://img2.baidu.com/it/u=4265225706,1640541864&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("Lululemon Align 瑜伽裤", "https://img2.baidu.com/it/u=3594144515,837873919&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667");
        IMG.put("Keep 瑜伽垫 TPE", "https://images.pexels.com/photos/6246682/pexels-photo-6246682.jpeg?w=400&h=300&fit=crop");
        IMG.put("Nike Pro 运动内衣", "https://t13.baidu.com/it/u=112317752,569248824&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("迪卡侬 MH500 登山包", "https://images.pexels.com/photos/1178525/pexels-photo-1178525.jpeg?w=400&h=300&fit=crop");
        IMG.put("The North Face 羽绒服 96Nuptse", "https://img2.baidu.com/it/u=1833124489,1439708820&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667");
        IMG.put("骆驼 露营帐篷 3-4人", "https://images.pexels.com/photos/17192955/pexels-photo-17192955.jpeg?w=400&h=300&fit=crop");
        IMG.put("Stanley 户外保温箱 15L", "https://images.pexels.com/photos/14974681/pexels-photo-14974681.jpeg?w=400&h=300&fit=crop");
        IMG.put("YETI Rambler 水壶 769ml", "https://images.pexels.com/photos/8266854/pexels-photo-8266854.jpeg?w=400&h=300&fit=crop");
        IMG.put("Peloton Bike+ 健身车", "https://images.pexels.com/photos/24244151/pexels-photo-24244151.jpeg?w=400&h=300&fit=crop");
        IMG.put("小米 走步机 C2", "https://img2.baidu.com/it/u=2895369781,3859827330&fm=253&fmt=auto&app=120&f=JPEG?w=760&h=760");
        IMG.put("Keep 智能跳绳", "https://t13.baidu.com/it/u=842414330,3255769619&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("哑铃套装 20kg 可调节", "https://images.pexels.com/photos/7743320/pexels-photo-7743320.jpeg?w=400&h=300&fit=crop");
        IMG.put("Nike 瑜伽训练垫 5mm", "https://img0.baidu.com/it/u=2676652242,2411546301&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=719");
        IMG.put("Garmin Fenix 7X Pro", "https://images.pexels.com/photos/3999644/pexels-photo-3999644.jpeg?w=400&h=300&fit=crop");
        IMG.put("佳明 Forerunner 265", "https://img1.baidu.com/it/u=3514582108,2762469287&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1067");
        IMG.put("Shimano RC7 骑行锁鞋", "https://images.pexels.com/photos/5807638/pexels-photo-5807638.jpeg?w=400&h=300&fit=crop");
        IMG.put("崔克 Domane AL 4", "https://img1.baidu.com/it/u=2945818137,1082357257&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1067");
        IMG.put("Speedo Fastskin 泳镜", "https://images.pexels.com/photos/28939350/pexels-photo-28939350.jpeg?w=400&h=300&fit=crop");
        IMG.put("Arena 游泳训练脚蹼", "https://t13.baidu.com/it/u=2101141292,2667305142&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("Wilson NBA 篮球", "https://images.pexels.com/photos/13179883/pexels-photo-13179883.jpeg?w=400&h=300&fit=crop");
        IMG.put("Yonex 天斧 AX100ZZ", "https://images.pexels.com/photos/11312132/pexels-photo-11312132.jpeg?w=400&h=300&fit=crop");
        IMG.put("Garmin Edge 540 码表", "https://img1.baidu.com/it/u=861522703,1135581721&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=667");
        IMG.put("兰蔻 极光水 150ml", "https://img2.baidu.com/it/u=2500045959,1603506790&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1039");
        IMG.put("SK-II 神仙水 230ml", "https://img0.baidu.com/it/u=3085469977,554236281&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1067");
        IMG.put("雅诗兰黛 小棕瓶精华 50ml", "https://images.pexels.com/photos/7321654/pexels-photo-7321654.jpeg?w=400&h=300&fit=crop");
        IMG.put("海蓝之谜 面霜 60ml", "https://images.pexels.com/photos/6635929/pexels-photo-6635929.jpeg?w=400&h=300&fit=crop");
        IMG.put("Olay 小白瓶 40ml", "https://t14.baidu.com/it/u=2678910981,1556072268&fm=224&app=112&f=JPEG?w=500&h=499");
        IMG.put("珀莱雅 双抗精华 30ml", "https://img0.baidu.com/it/u=3477624241,339296795&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=889");
        IMG.put("迪奥 口红 999 哑光", "https://images.pexels.com/photos/6648498/pexels-photo-6648498.jpeg?w=400&h=300&fit=crop");
        IMG.put("Tom Ford 黑金唇膏 16", "https://img2.baidu.com/it/u=121355351,2212544591&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=774");
        IMG.put("Charlotte Tilbury 枕边话", "https://t15.baidu.com/it/u=2810088560,3233639926&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("YSL 小金条 #21", "https://img1.baidu.com/it/u=292920495,3787263984&fm=253&fmt=auto&app=138&f=JPEG?w=924&h=800");
        IMG.put("MAC 生姜高光 DoubleGleam", "https://img0.baidu.com/it/u=1455105527,2632334290&fm=253&fmt=auto&app=138&f=JPEG?w=514&h=500");
        IMG.put("Jo Malone 蓝风铃 30ml", "https://img1.baidu.com/it/u=4261862670,2492828225&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=613");
        IMG.put("Diptyque 檀道 75ml", "https://images.pexels.com/photos/2866796/pexels-photo-2866796.jpeg?w=400&h=300&fit=crop");
        IMG.put("潘海利根 兽首狐狸 75ml", "https://img1.baidu.com/it/u=92314570,2067180411&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("Aesop 赋活芳香护手霜 75ml", "https://images.pexels.com/photos/17788297/pexels-photo-17788297.jpeg?w=400&h=300&fit=crop");
        IMG.put("卡诗 白金赋活洗发水 250ml", "https://t14.baidu.com/it/u=1654641394,3745738925&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("戴森 Supersonic 吹风机", "https://img1.baidu.com/it/u=23607614,1665746621&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=654");
        IMG.put("Marvis 牙膏套装 7支", "https://images.pexels.com/photos/3735649/pexels-photo-3735649.jpeg?w=400&h=300&fit=crop");
        IMG.put("CeraVe 保湿洁面乳 473ml", "https://images.pexels.com/photos/14107358/pexels-photo-14107358.jpeg?w=400&h=300&fit=crop");
        IMG.put("修丽可 CF 抗氧化精华 30ml", "https://t15.baidu.com/it/u=2862061342,1359681833&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("三只松鼠 每日坚果 750g", "https://images.pexels.com/photos/5507656/pexels-photo-5507656.jpeg?w=400&h=300&fit=crop");
        IMG.put("良品铺子 猪肉脯 500g", "https://images.pexels.com/photos/5237009/pexels-photo-5237009.jpeg?w=400&h=300&fit=crop");
        IMG.put("瑞士莲 软心巧克力 600g", "https://images.pexels.com/photos/6167328/pexels-photo-6167328.jpeg?w=400&h=300&fit=crop");
        IMG.put("illy 深度烘焙咖啡粉 250g", "https://images.pexels.com/photos/27528587/pexels-photo-27528587.jpeg?w=400&h=300&fit=crop");
        IMG.put("瑞幸 冻干咖啡粉 12颗", "https://img0.baidu.com/it/u=191208523,2208279420&fm=253&fmt=auto&app=138&f=JPEG?w=667&h=500");
        IMG.put("永璞 闪萃咖啡液 14杯", "https://t14.baidu.com/it/u=133397750,3904153112&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("认养一头牛 纯牛奶 250ml×24盒", "https://t15.baidu.com/it/u=2261298056,2831031781&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("安佳 全脂奶粉 1kg", "https://t14.baidu.com/it/u=856171722,2999156335&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("北海牧场 酸奶 200g×12杯", "https://img0.baidu.com/it/u=3735118658,3701049556&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=500");
        IMG.put("桂格 即食燕麦片 1.5kg", "https://t15.baidu.com/it/u=3884123748,36247541&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("汤臣倍健 蛋白粉 450g", "https://t13.baidu.com/it/u=182116611,1399511843&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("东方树叶 茉莉花茶 500ml×15", "https://img2.baidu.com/it/u=4238658415,1843742652&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1220");
        IMG.put("农夫山泉 矿泉水 550ml×24", "https://t14.baidu.com/it/u=2904280698,2988415572&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("青岛啤酒 经典1903 500ml×12", "https://img1.baidu.com/it/u=3023766857,170739254&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("奔富 Bin 389 干红 750ml", "https://img1.baidu.com/it/u=3331296355,1125988652&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500");
        IMG.put("锐澳 微醺鸡尾酒 330ml×8", "https://img1.baidu.com/it/u=3046731875,2649461416&fm=253&fmt=auto&app=138&f=JPEG?w=535&h=500");
        IMG.put("茅台 飞天53度 500ml", "https://img2.baidu.com/it/u=3503855494,4060913369&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=674");
        IMG.put("奈雪的茶 茉莉初雪 茶包 15袋", "https://t13.baidu.com/it/u=3837628902,3119685359&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("小猪呵呵 午餐肉 340g", "https://t14.baidu.com/it/u=2402713266,3628962914&fm=224&app=112&f=JPEG?w=500&h=500");
        IMG.put("沃隆 每日坚果 礼盒装 1kg", "https://img2.baidu.com/it/u=2219028154,3222013428&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=1067");
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
            productRepository.save(product(categories.get("phone"), "iphone-15-pro-max", "iPhone 15 Pro Max", "iPhone 15 Pro Max", "Brand", "7666", 39, 79));
            productRepository.save(product(categories.get("phone"), "华为-mate-60-pro", "华为 Mate 60 Pro", "华为 Mate 60 Pro", "Brand", "3923.9", 21, 82));
            productRepository.save(product(categories.get("phone"), "小米-14-ultra", "小米 14 Ultra", "小米 14 Ultra", "Brand", "7935", 26, 68));
            productRepository.save(product(categories.get("phone"), "oppo-find-x7-ultra", "OPPO Find X7 Ultra", "OPPO Find X7 Ultra", "Brand", "8337.8", 50, 79));
            productRepository.save(product(categories.get("phone"), "vivo-x100-pro", "vivo X100 Pro", "vivo X100 Pro", "Brand", "8961.9", 24, 44));
            productRepository.save(product(categories.get("phone"), "三星-galaxy-s24-ultra", "三星 Galaxy S24 Ultra", "三星 Galaxy S24 Ultra", "Brand", "8207", 27, 41));
            productRepository.save(product(categories.get("phone"), "一加-12", "一加 12", "一加 12", "Brand", "8002", 15, 210));
            productRepository.save(product(categories.get("phone"), "荣耀-magic6-pro", "荣耀 Magic6 Pro", "荣耀 Magic6 Pro", "Brand", "4611.8", 36, 92));
            productRepository.save(product(categories.get("phone"), "nova-12-ultra", "Nova 12 Ultra", "Nova 12 Ultra", "Brand", "8104", 38, 112));
            productRepository.save(product(categories.get("phone"), "airpods-pro-2", "AirPods Pro 2", "AirPods Pro 2", "Brand", "5462", 19, 265));
            productRepository.save(product(categories.get("phone"), "华为-freebuds-pro-3", "华为 FreeBuds Pro 3", "华为 FreeBuds Pro 3", "Brand", "563.9", 46, 258));
            productRepository.save(product(categories.get("phone"), "小米-buds-4-pro", "小米 Buds 4 Pro", "小米 Buds 4 Pro", "Brand", "1924", 57, 339));
            productRepository.save(product(categories.get("phone"), "apple-watch-ultra-2", "Apple Watch Ultra 2", "Apple Watch Ultra 2", "Brand", "8583", 14, 33));
            productRepository.save(product(categories.get("phone"), "华为-watch-gt-4", "华为 Watch GT 4", "华为 Watch GT 4", "Brand", "630.9", 31, 314));
            productRepository.save(product(categories.get("phone"), "小米手环-8-pro", "小米手环 8 Pro", "小米手环 8 Pro", "Brand", "1853", 88, 241));
            productRepository.save(product(categories.get("phone"), "ipad-air-m2", "iPad Air M2", "iPad Air M2", "Brand", "3819.8", 23, 211));
            productRepository.save(product(categories.get("phone"), "华为-matepad-pro-132", "华为 MatePad Pro 13.2", "华为 MatePad Pro 13.2", "Brand", "2818", 30, 107));
            productRepository.save(product(categories.get("phone"), "小米平板-6s-pro", "小米平板 6S Pro", "小米平板 6S Pro", "Brand", "1889", 31, 92));
            productRepository.save(product(categories.get("phone"), "anker-65w-氮化镓充电器", "ANKER 65W 氮化镓充电器", "ANKER 65W 氮化镓充电器", "Brand", "2031", 51, 173));
            productRepository.save(product(categories.get("phone"), "闪迪-256gb-tf-存储卡", "闪迪 256GB TF 存储卡", "闪迪 256GB TF 存储卡", "Brand", "8528.5", 125, 758));
            productRepository.save(product(categories.get("phone"), "sony-wh-1000xm5", "Sony WH-1000XM5", "Sony WH-1000XM5", "Brand", "3108.5", 17, 198));
            productRepository.save(product(categories.get("phone"), "bose-qc-ultra", "Bose QC Ultra", "Bose QC Ultra", "Brand", "7275.5", 30, 188));
            productRepository.save(product(categories.get("phone"), "jbl-flip-6", "JBL Flip 6", "JBL Flip 6", "Brand", "3344.9", 22, 267));
            productRepository.save(product(categories.get("phone"), "kindle-paperwhite-5", "Kindle Paperwhite 5", "Kindle Paperwhite 5", "Brand", "8884.9", 116, 200));
            productRepository.save(product(categories.get("phone"), "gopro-hero-12-black", "GoPro Hero 12 Black", "GoPro Hero 12 Black", "Brand", "6981", 38, 384));
            productRepository.save(product(categories.get("phone"), "insta360-x4", "Insta360 X4", "Insta360 X4", "Brand", "8694.9", 20, 257));
            productRepository.save(product(categories.get("phone"), "macbook-pro-14-m3", "MacBook Pro 14 M3", "MacBook Pro 14 M3", "Brand", "6675", 18, 29));
            productRepository.save(product(categories.get("phone"), "华为-matebook-x-pro-2024", "华为 MateBook X Pro 2024", "华为 MateBook X Pro 2024", "Brand", "3942.5", 11, 137));
            productRepository.save(product(categories.get("phone"), "小米-book-pro-16", "小米 Book Pro 16", "小米 Book Pro 16", "Brand", "1079", 11, 95));
            productRepository.save(product(categories.get("phone"), "samsung-viewfinity-s9", "Samsung ViewFinity S9", "Samsung ViewFinity S9", "Brand", "8180.9", 28, 54));
            productRepository.save(product(categories.get("phone"), "罗技-g-pro-x-耳机", "罗技 G Pro X 耳机", "罗技 G Pro X 耳机", "Brand", "1182.9", 49, 177));
            productRepository.save(product(categories.get("phone"), "samsung-t7-shield-2tb", "Samsung T7 Shield 2TB", "Samsung T7 Shield 2TB", "Brand", "4030.9", 114, 447));
            productRepository.save(product(categories.get("phone"), "华为-ax6", "华为 AX6", "华为 AX6", "Brand", "1452.9", 119, 454));
            productRepository.save(product(categories.get("phone"), "小米-扫拖机器人-x20", "小米 扫拖机器人 X20+", "小米 扫拖机器人 X20+", "Brand", "4696", 37, 88));
            productRepository.save(product(categories.get("phone"), "bose-soundlink-max", "Bose SoundLink Max", "Bose SoundLink Max", "Brand", "523", 148, 481));
            productRepository.save(product(categories.get("phone"), "小米-空气净化器-4-pro", "小米 空气净化器 4 Pro", "小米 空气净化器 4 Pro", "Brand", "2568", 15, 139));
            productRepository.save(product(categories.get("phone"), "小米-电热水壶-2", "小米 电热水壶 2", "小米 电热水壶 2", "Brand", "3022.9", 74, 416));
            productRepository.save(product(categories.get("phone"), "小米-走步机-c2", "小米 走步机 C2", "小米 走步机 C2", "Brand", "208", 81, 182));
            // 电脑办公 (19件)
            productRepository.save(product(categories.get("computer"), "绿联-手机支架-桌面", "绿联 手机支架 桌面", "绿联 手机支架 桌面", "Brand", "5349.5", 72, 149));
            productRepository.save(product(categories.get("computer"), "thinkpad-x1-carbon-gen-12", "ThinkPad X1 Carbon Gen 12", "ThinkPad X1 Carbon Gen 12", "Brand", "12001.8", 8, 38));
            productRepository.save(product(categories.get("computer"), "华硕-rog-枪神8", "华硕 ROG 枪神8", "华硕 ROG 枪神8", "Brand", "14171.9", 36, 40));
            productRepository.save(product(categories.get("computer"), "dell-u2724d", "Dell U2724D", "Dell U2724D", "Brand", "2685.5", 30, 263));
            productRepository.save(product(categories.get("computer"), "lg-27gr95um", "LG 27GR95UM", "LG 27GR95UM", "Brand", "2543", 26, 111));
            productRepository.save(product(categories.get("computer"), "logitech-mx-master-3s", "Logitech MX Master 3S", "Logitech MX Master 3S", "Brand", "10016", 87, 151));
            productRepository.save(product(categories.get("computer"), "keychron-q1-pro", "Keychron Q1 Pro", "Keychron Q1 Pro", "Brand", "10507.5", 54, 177));
            productRepository.save(product(categories.get("computer"), "cherry-mx-board-30s", "Cherry MX Board 3.0S", "Cherry MX Board 3.0S", "Brand", "4928.9", 132, 135));
            productRepository.save(product(categories.get("computer"), "blue-yeti-x-usb-麦克风", "Blue Yeti X USB 麦克风", "Blue Yeti X USB 麦克风", "Brand", "14301.8", 37, 286));
            productRepository.save(product(categories.get("computer"), "wd-my-book-12tb", "WD My Book 12TB", "WD My Book 12TB", "Brand", "14991", 116, 414));
            productRepository.save(product(categories.get("computer"), "tp-link-axe5400", "TP-Link AXE5400", "TP-Link AXE5400", "Brand", "2966.5", 140, 453));
            productRepository.save(product(categories.get("computer"), "明基-screenbar-halo", "明基 ScreenBar Halo", "明基 ScreenBar Halo", "Brand", "8240.9", 139, 205));
            productRepository.save(product(categories.get("computer"), "爱格升-lx-显示器支架", "爱格升 LX 显示器支架", "爱格升 LX 显示器支架", "Brand", "11830", 168, 288));
            productRepository.save(product(categories.get("computer"), "herman-miller-aeron", "Herman Miller Aeron", "Herman Miller Aeron", "Brand", "14956.9", 20, 39));
            productRepository.save(product(categories.get("computer"), "网易严选-人体工学椅", "网易严选 人体工学椅", "网易严选 人体工学椅", "Brand", "7472", 23, 136));
            productRepository.save(product(categories.get("computer"), "wacom-intuos-pro-m", "Wacom Intuos Pro M", "Wacom Intuos Pro M", "Brand", "7162", 51, 187));
            productRepository.save(product(categories.get("computer"), "apc-back-ups-650", "APC Back-UPS 650", "APC Back-UPS 650", "Brand", "1866", 181, 629));
            productRepository.save(product(categories.get("computer"), "绿联-雷电4-扩展坞", "绿联 雷电4 扩展坞", "绿联 雷电4 扩展坞", "Brand", "10540", 88, 223));
            productRepository.save(product(categories.get("computer"), "微软-surface-pro-10", "微软 Surface Pro 10", "微软 Surface Pro 10", "Brand", "4410.9", 12, 146));
            // 家居生活 (33件)
            productRepository.save(product(categories.get("home"), "大疆-osmo-mobile-6", "大疆 Osmo Mobile 6", "大疆 Osmo Mobile 6", "Brand", "4457.9", 38, 96));
            productRepository.save(product(categories.get("home"), "雷蛇-deathadder-v3-pro", "雷蛇 DeathAdder V3 Pro", "雷蛇 DeathAdder V3 Pro", "Brand", "997.8", 110, 110));
            productRepository.save(product(categories.get("home"), "群晖-ds224", "群晖 DS224+", "群晖 DS224+", "Brand", "153.8", 15, 58));
            productRepository.save(product(categories.get("home"), "罗技-streamcam", "罗技 StreamCam", "罗技 StreamCam", "Brand", "2118.5", 114, 170));
            productRepository.save(product(categories.get("home"), "戴森-v15-detect", "戴森 V15 Detect", "戴森 V15 Detect", "Brand", "3893.5", 16, 190));
            productRepository.save(product(categories.get("home"), "戴森-airwrap-hs05", "戴森 Airwrap HS05", "戴森 Airwrap HS05", "Brand", "3185.8", 29, 56));
            productRepository.save(product(categories.get("home"), "飞利浦-空气炸锅-hd9651", "飞利浦 空气炸锅 HD9651", "飞利浦 空气炸锅 HD9651", "Brand", "579.5", 87, 141));
            productRepository.save(product(categories.get("home"), "德龙-ecam23460-全自动咖啡机", "德龙 ECAM23.460 全自动咖啡机", "德龙 ECAM23.460 全自动咖啡机", "Brand", "3517.8", 43, 270));
            productRepository.save(product(categories.get("home"), "松下-sr-hbc184-电饭煲", "松下 SR-HBC184 电饭煲", "松下 SR-HBC184 电饭煲", "Brand", "1369.9", 34, 142));
            productRepository.save(product(categories.get("home"), "九阳-dj13b-d08ec-豆浆机", "九阳 DJ13B-D08EC 豆浆机", "九阳 DJ13B-D08EC 豆浆机", "Brand", "957", 59, 91));
            productRepository.save(product(categories.get("home"), "戴森-pure-cool-空气净化风扇", "戴森 Pure Cool 空气净化风扇", "戴森 Pure Cool 空气净化风扇", "Brand", "444.9", 28, 277));
            productRepository.save(product(categories.get("home"), "aqara-智能窗帘电机-c3", "Aqara 智能窗帘电机 C3", "Aqara 智能窗帘电机 C3", "Brand", "4101", 97, 390));
            productRepository.save(product(categories.get("home"), "yeelight-智能吸顶灯-pro", "Yeelight 智能吸顶灯 Pro", "Yeelight 智能吸顶灯 Pro", "Brand", "1192.5", 50, 82));
            productRepository.save(product(categories.get("home"), "德业-dyd-t22a3-除湿机", "德业 DYD-T22A3 除湿机", "德业 DYD-T22A3 除湿机", "Brand", "3024", 114, 323));
            productRepository.save(product(categories.get("home"), "muji-超声波香薰机", "MUJI 超声波香薰机", "MUJI 超声波香薰机", "Brand", "4839.5", 173, 381));
            productRepository.save(product(categories.get("home"), "象印-sm-sg48-保温杯", "象印 SM-SG48 保温杯", "象印 SM-SG48 保温杯", "Brand", "4847.5", 19, 234));
            productRepository.save(product(categories.get("home"), "水星家纺-95鹅绒被", "水星家纺 95鹅绒被", "水星家纺 95鹅绒被", "Brand", "2211.9", 81, 647));
            productRepository.save(product(categories.get("home"), "天堂-晴雨伞-三折", "天堂 晴雨伞 三折", "天堂 晴雨伞 三折", "Brand", "4657.5", 93, 421));
            productRepository.save(product(categories.get("home"), "3m-净水器-sdw8000", "3M 净水器 SDW8000", "3M 净水器 SDW8000", "Brand", "2079.9", 89, 106));
            productRepository.save(product(categories.get("home"), "bruno-多功能料理锅", "Bruno 多功能料理锅", "Bruno 多功能料理锅", "Brand", "2228", 88, 521));
            productRepository.save(product(categories.get("home"), "美的-m1-l213c-微波炉", "美的 M1-L213C 微波炉", "美的 M1-L213C 微波炉", "Brand", "3817", 35, 165));
            productRepository.save(product(categories.get("home"), "安踏-c37-50-休闲鞋", "安踏 C37 5.0 休闲鞋", "安踏 C37 5.0 休闲鞋", "Brand", "4148.5", 147, 80));
            productRepository.save(product(categories.get("home"), "stanley-户外保温箱-15l", "Stanley 户外保温箱 15L", "Stanley 户外保温箱 15L", "Brand", "2636", 99, 478));
            productRepository.save(product(categories.get("home"), "佳明-forerunner-265", "佳明 Forerunner 265", "佳明 Forerunner 265", "Brand", "2382.9", 77, 470));
            productRepository.save(product(categories.get("home"), "崔克-domane-al-4", "崔克 Domane AL 4", "崔克 Domane AL 4", "Brand", "1787.9", 40, 65));
            productRepository.save(product(categories.get("home"), "charlotte-tilbury-枕边话", "Charlotte Tilbury 枕边话", "Charlotte Tilbury 枕边话", "Brand", "2252", 220, 455));
            productRepository.save(product(categories.get("home"), "潘海利根-兽首狐狸-75ml", "潘海利根 兽首狐狸 75ml", "潘海利根 兽首狐狸 75ml", "Brand", "2643.5", 52, 339));
            productRepository.save(product(categories.get("home"), "marvis-牙膏套装-7支", "Marvis 牙膏套装 7支", "Marvis 牙膏套装 7支", "Brand", "321.9", 192, 402));
            productRepository.save(product(categories.get("home"), "illy-深度烘焙咖啡粉-250g", "illy 深度烘焙咖啡粉 250g", "illy 深度烘焙咖啡粉 250g", "Brand", "517", 176, 153));
            productRepository.save(product(categories.get("home"), "瑞幸-冻干咖啡粉-12颗", "瑞幸 冻干咖啡粉 12颗", "瑞幸 冻干咖啡粉 12颗", "Brand", "2715.9", 250, 1190));
            productRepository.save(product(categories.get("home"), "永璞-闪萃咖啡液-14杯", "永璞 闪萃咖啡液 14杯", "永璞 闪萃咖啡液 14杯", "Brand", "4621", 224, 498));
            productRepository.save(product(categories.get("home"), "安佳-全脂奶粉-1kg", "安佳 全脂奶粉 1kg", "安佳 全脂奶粉 1kg", "Brand", "2270.9", 196, 282));
            productRepository.save(product(categories.get("home"), "奔富-bin-389-干红-750ml", "奔富 Bin 389 干红 750ml", "奔富 Bin 389 干红 750ml", "Brand", "3845.5", 22, 214));
            // 运动户外 (21件)
            productRepository.save(product(categories.get("sport"), "nike-air-zoom-pegasus-40", "Nike Air Zoom Pegasus 40", "Nike Air Zoom Pegasus 40", "Brand", "2054", 100, 85));
            productRepository.save(product(categories.get("sport"), "adidas-ultraboost-light", "Adidas Ultraboost Light", "Adidas Ultraboost Light", "Brand", "6186.9", 89, 350));
            productRepository.save(product(categories.get("sport"), "李宁-超轻21-跑鞋", "李宁 超轻21 跑鞋", "李宁 超轻21 跑鞋", "Brand", "5250", 158, 540));
            productRepository.save(product(categories.get("sport"), "lululemon-align-瑜伽裤", "Lululemon Align 瑜伽裤", "Lululemon Align 瑜伽裤", "Brand", "633.8", 84, 385));
            productRepository.save(product(categories.get("sport"), "keep-瑜伽垫-tpe", "Keep 瑜伽垫 TPE", "Keep 瑜伽垫 TPE", "Brand", "5648.8", 50, 485));
            productRepository.save(product(categories.get("sport"), "nike-pro-运动内衣", "Nike Pro 运动内衣", "Nike Pro 运动内衣", "Brand", "2236.9", 22, 79));
            productRepository.save(product(categories.get("sport"), "迪卡侬-mh500-登山包", "迪卡侬 MH500 登山包", "迪卡侬 MH500 登山包", "Brand", "1765.8", 13, 27));
            productRepository.save(product(categories.get("sport"), "the-north-face-羽绒服-96nuptse", "The North Face 羽绒服 96Nuptse", "The North Face 羽绒服 96Nuptse", "Brand", "3081", 23, 170));
            productRepository.save(product(categories.get("sport"), "骆驼-露营帐篷-3-4人", "骆驼 露营帐篷 3-4人", "骆驼 露营帐篷 3-4人", "Brand", "3037", 40, 132));
            productRepository.save(product(categories.get("sport"), "yeti-rambler-水壶-769ml", "YETI Rambler 水壶 769ml", "YETI Rambler 水壶 769ml", "Brand", "5256.9", 168, 408));
            productRepository.save(product(categories.get("sport"), "peloton-bike-健身车", "Peloton Bike+ 健身车", "Peloton Bike+ 健身车", "Brand", "5137.5", 49, 107));
            productRepository.save(product(categories.get("sport"), "keep-智能跳绳", "Keep 智能跳绳", "Keep 智能跳绳", "Brand", "876.5", 128, 771));
            productRepository.save(product(categories.get("sport"), "哑铃套装-20kg-可调节", "哑铃套装 20kg 可调节", "哑铃套装 20kg 可调节", "Brand", "1956.9", 48, 215));
            productRepository.save(product(categories.get("sport"), "nike-瑜伽训练垫-5mm", "Nike 瑜伽训练垫 5mm", "Nike 瑜伽训练垫 5mm", "Brand", "2419", 143, 409));
            productRepository.save(product(categories.get("sport"), "garmin-fenix-7x-pro", "Garmin Fenix 7X Pro", "Garmin Fenix 7X Pro", "Brand", "1915.9", 21, 142));
            productRepository.save(product(categories.get("sport"), "shimano-rc7-骑行锁鞋", "Shimano RC7 骑行锁鞋", "Shimano RC7 骑行锁鞋", "Brand", "618", 114, 730));
            productRepository.save(product(categories.get("sport"), "speedo-fastskin-泳镜", "Speedo Fastskin 泳镜", "Speedo Fastskin 泳镜", "Brand", "1624.9", 140, 612));
            productRepository.save(product(categories.get("sport"), "arena-游泳训练脚蹼", "Arena 游泳训练脚蹼", "Arena 游泳训练脚蹼", "Brand", "3011.8", 233, 289));
            productRepository.save(product(categories.get("sport"), "wilson-nba-篮球", "Wilson NBA 篮球", "Wilson NBA 篮球", "Brand", "5198.8", 146, 284));
            productRepository.save(product(categories.get("sport"), "yonex-天斧-ax100zz", "Yonex 天斧 AX100ZZ", "Yonex 天斧 AX100ZZ", "Brand", "5199.9", 71, 473));
            productRepository.save(product(categories.get("sport"), "garmin-edge-540-码表", "Garmin Edge 540 码表", "Garmin Edge 540 码表", "Brand", "694.5", 25, 71));
            // 美妆个护 (17件)
            productRepository.save(product(categories.get("beauty"), "兰蔻-极光水-150ml", "兰蔻 极光水 150ml", "兰蔻 极光水 150ml", "Brand", "3796.5", 50, 316));
            productRepository.save(product(categories.get("beauty"), "sk-ii-神仙水-230ml", "SK-II 神仙水 230ml", "SK-II 神仙水 230ml", "Brand", "2215", 98, 453));
            productRepository.save(product(categories.get("beauty"), "雅诗兰黛-小棕瓶精华-50ml", "雅诗兰黛 小棕瓶精华 50ml", "雅诗兰黛 小棕瓶精华 50ml", "Brand", "3007.5", 107, 405));
            productRepository.save(product(categories.get("beauty"), "海蓝之谜-面霜-60ml", "海蓝之谜 面霜 60ml", "海蓝之谜 面霜 60ml", "Brand", "3324", 58, 205));
            productRepository.save(product(categories.get("beauty"), "olay-小白瓶-40ml", "Olay 小白瓶 40ml", "Olay 小白瓶 40ml", "Brand", "2491", 113, 423));
            productRepository.save(product(categories.get("beauty"), "珀莱雅-双抗精华-30ml", "珀莱雅 双抗精华 30ml", "珀莱雅 双抗精华 30ml", "Brand", "3490", 101, 529));
            productRepository.save(product(categories.get("beauty"), "迪奥-口红-999-哑光", "迪奥 口红 999 哑光", "迪奥 口红 999 哑光", "Brand", "415", 176, 800));
            productRepository.save(product(categories.get("beauty"), "tom-ford-黑金唇膏-16", "Tom Ford 黑金唇膏 16", "Tom Ford 黑金唇膏 16", "Brand", "398.5", 85, 539));
            productRepository.save(product(categories.get("beauty"), "ysl-小金条-21", "YSL 小金条 #21", "YSL 小金条 #21", "Brand", "1397", 67, 120));
            productRepository.save(product(categories.get("beauty"), "mac-生姜高光-doublegleam", "MAC 生姜高光 DoubleGleam", "MAC 生姜高光 DoubleGleam", "Brand", "900.9", 71, 383));
            productRepository.save(product(categories.get("beauty"), "jo-malone-蓝风铃-30ml", "Jo Malone 蓝风铃 30ml", "Jo Malone 蓝风铃 30ml", "Brand", "1157", 54, 440));
            productRepository.save(product(categories.get("beauty"), "diptyque-檀道-75ml", "Diptyque 檀道 75ml", "Diptyque 檀道 75ml", "Brand", "1750", 109, 265));
            productRepository.save(product(categories.get("beauty"), "aesop-赋活芳香护手霜-75ml", "Aesop 赋活芳香护手霜 75ml", "Aesop 赋活芳香护手霜 75ml", "Brand", "672.9", 178, 383));
            productRepository.save(product(categories.get("beauty"), "卡诗-白金赋活洗发水-250ml", "卡诗 白金赋活洗发水 250ml", "卡诗 白金赋活洗发水 250ml", "Brand", "3127.9", 159, 417));
            productRepository.save(product(categories.get("beauty"), "戴森-supersonic-吹风机", "戴森 Supersonic 吹风机", "戴森 Supersonic 吹风机", "Brand", "3579", 123, 315));
            productRepository.save(product(categories.get("beauty"), "cerave-保湿洁面乳-473ml", "CeraVe 保湿洁面乳 473ml", "CeraVe 保湿洁面乳 473ml", "Brand", "1158.9", 167, 421));
            productRepository.save(product(categories.get("beauty"), "修丽可-cf-抗氧化精华-30ml", "修丽可 CF 抗氧化精华 30ml", "修丽可 CF 抗氧化精华 30ml", "Brand", "1858.8", 21, 131));
            // 食品饮料 (15件)
            productRepository.save(product(categories.get("food"), "三只松鼠-每日坚果-750g", "三只松鼠 每日坚果 750g", "三只松鼠 每日坚果 750g", "Brand", "1727", 152, 489));
            productRepository.save(product(categories.get("food"), "良品铺子-猪肉脯-500g", "良品铺子 猪肉脯 500g", "良品铺子 猪肉脯 500g", "Brand", "2881.5", 179, 740));
            productRepository.save(product(categories.get("food"), "瑞士莲-软心巧克力-600g", "瑞士莲 软心巧克力 600g", "瑞士莲 软心巧克力 600g", "Brand", "2941.8", 316, 996));
            productRepository.save(product(categories.get("food"), "认养一头牛-纯牛奶-250ml24盒", "认养一头牛 纯牛奶 250ml×24盒", "认养一头牛 纯牛奶 250ml×24盒", "Brand", "2307", 219, 701));
            productRepository.save(product(categories.get("food"), "北海牧场-酸奶-200g12杯", "北海牧场 酸奶 200g×12杯", "北海牧场 酸奶 200g×12杯", "Brand", "2474", 235, 751));
            productRepository.save(product(categories.get("food"), "桂格-即食燕麦片-15kg", "桂格 即食燕麦片 1.5kg", "桂格 即食燕麦片 1.5kg", "Brand", "961.5", 246, 592));
            productRepository.save(product(categories.get("food"), "汤臣倍健-蛋白粉-450g", "汤臣倍健 蛋白粉 450g", "汤臣倍健 蛋白粉 450g", "Brand", "392", 94, 288));
            productRepository.save(product(categories.get("food"), "东方树叶-茉莉花茶-500ml15", "东方树叶 茉莉花茶 500ml×15", "东方树叶 茉莉花茶 500ml×15", "Brand", "139", 105, 435));
            productRepository.save(product(categories.get("food"), "农夫山泉-矿泉水-550ml24", "农夫山泉 矿泉水 550ml×24", "农夫山泉 矿泉水 550ml×24", "Brand", "2427.5", 214, 177));
            productRepository.save(product(categories.get("food"), "青岛啤酒-经典1903-500ml12", "青岛啤酒 经典1903 500ml×12", "青岛啤酒 经典1903 500ml×12", "Brand", "2242", 211, 776));
            productRepository.save(product(categories.get("food"), "锐澳-微醺鸡尾酒-330ml8", "锐澳 微醺鸡尾酒 330ml×8", "锐澳 微醺鸡尾酒 330ml×8", "Brand", "428.8", 34, 95));
            productRepository.save(product(categories.get("food"), "茅台-飞天53度-500ml", "茅台 飞天53度 500ml", "茅台 飞天53度 500ml", "Brand", "923", 20, 191));
            productRepository.save(product(categories.get("food"), "奈雪的茶-茉莉初雪-茶包-15袋", "奈雪的茶 茉莉初雪 茶包 15袋", "奈雪的茶 茉莉初雪 茶包 15袋", "Brand", "1135", 157, 404));
            productRepository.save(product(categories.get("food"), "小猪呵呵-午餐肉-340g", "小猪呵呵 午餐肉 340g", "小猪呵呵 午餐肉 340g", "Brand", "111.9", 214, 585));
            productRepository.save(product(categories.get("food"), "沃隆-每日坚果-礼盒装-1kg", "沃隆 每日坚果 礼盒装 1kg", "沃隆 每日坚果 礼盒装 1kg", "Brand", "2628", 112, 863));
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
