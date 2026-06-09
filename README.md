# ShoppingSystem 电商购物系统

ShoppingSystem 是一个前后端分离的电商购物系统，包含商品浏览、分类查询、购物车、下单、订单支付、用户登录注册、管理端商品/分类/订单维护，以及基于用户行为的商品推荐能力。

项目后端使用 Spring Boot，前端使用 Vue 3 + Vite。默认启动时使用 H2 本地演示数据库，并自动初始化示例用户、商品、分类和推荐行为数据，适合本地开发、课程作业演示和功能验证。

## 技术栈

后端：

- Java 17
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Data JPA
- Bean Validation
- H2 数据库
- MySQL 驱动
- Maven

前端：

- Vue 3
- Vite
- Vue Router
- Pinia
- Axios

## 项目结构

```text
ShoppingSystem/
├── backend/                 后端 Spring Boot 项目
│   ├── src/main/java/com/wms/shoppingsys/
│   │   ├── auth/            鉴权基础设施，如拦截器、当前用户、Token 存储
│   │   ├── common/          通用响应、业务异常、错误码、全局异常处理
│   │   ├── config/          应用配置、初始化数据
│   │   ├── controller/      普通用户接口 Controller
│   │   │   └── admin/       管理端接口 Controller
│   │   ├── dto/             请求和响应 DTO
│   │   ├── entity/          JPA 实体
│   │   ├── enums/           业务枚举
│   │   ├── repository/      JPA Repository
│   │   └── service/         业务服务、推荐服务、行为记录服务
│   └── src/main/resources/  应用配置文件
├── frontend/                前端 Vue 项目
│   └── src/
│       ├── api/             Axios 请求封装
│       ├── components/      通用组件
│       ├── router/          路由配置
│       ├── stores/          Pinia 状态管理
│       └── views/           页面视图
│           ├── admin/       管理端页面
│           └── shop/        商城端页面
└── docs/                    设计文档和实现计划
```

## 功能模块

- 用户认证：注册、登录、Token 鉴权、普通用户和管理员权限区分
- 商品浏览：商品列表、商品详情、相似商品、分类筛选、关键词搜索
- 分类管理：商城端分类展示，管理端分类新增、编辑、禁用
- 购物车：添加商品、修改数量、删除商品、查看购物车
- 订单：创建订单、查看订单、支付订单
- 管理端：商品维护、分类维护、订单状态维护、用户推荐查看
- 推荐系统：记录浏览、加购、下单行为，使用基于物品的协同过滤生成推荐

## 快速启动

### 1. 启动后端

进入后端目录：

```bash
cd backend
```

运行测试：

```bash
mvn test
```

启动服务：

```bash
mvn spring-boot:run
```

后端默认监听：

```text
http://localhost:8080
```

默认启用 `demo` profile，使用 H2 文件数据库：

```text
backend/target/demo-db
```

演示数据由 `DataInitializer` 自动初始化。

### 2. 启动前端

进入前端目录：

```bash
cd frontend
```

安装依赖：

```bash
npm install
```

启动开发服务：

```bash
npm run dev
```

前端默认监听：

```text
http://127.0.0.1:5173
```

前端默认请求后端地址：

```text
http://localhost:8080/api
```

如需修改接口地址，可以设置环境变量：

```bash
VITE_API_BASE_URL=http://localhost:8080/api npm run dev
```

## 默认账号

演示模式会初始化以下账号：

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| admin | admin123 | 管理员 |
| alice | pass123 | 普通用户 |
| bob | pass123 | 普通用户 |
| carol | pass123 | 普通用户 |

## 主要接口

用户端接口：

```text
POST   /api/auth/register
POST   /api/auth/login
GET    /api/categories
GET    /api/products
GET    /api/products/{id}
GET    /api/products/{id}/similar
GET    /api/cart
POST   /api/cart/items
PUT    /api/cart/items/{id}
DELETE /api/cart/items/{id}
GET    /api/orders
POST   /api/orders
GET    /api/orders/{id}
POST   /api/orders/{id}/pay
GET    /api/recommendations/home
GET    /api/recommendations/cart
```

管理端接口：

```text
GET    /api/admin/products
POST   /api/admin/products
PUT    /api/admin/products/{id}
POST   /api/admin/products/{id}/on-sale
POST   /api/admin/products/{id}/off-sale
GET    /api/admin/categories
POST   /api/admin/categories
PUT    /api/admin/categories/{id}
POST   /api/admin/categories/{id}/disable
GET    /api/admin/orders
GET    /api/admin/orders/{id}
PUT    /api/admin/orders/{id}/status
GET    /api/admin/recommendations/users/{userId}
```

需要登录的接口使用 Bearer Token：

```text
Authorization: Bearer <token>
```

## 数据库配置

默认配置位于：

```text
backend/src/main/resources/application.properties
backend/src/main/resources/application-demo.properties
backend/src/main/resources/application-mysql.properties
```

默认运行模式：

```properties
spring.profiles.active=demo
```

如需使用 MySQL，可以启用 `mysql` profile，并根据本机环境修改 `application-mysql.properties` 中的数据库地址、用户名和密码。

示例：

```bash
SPRING_PROFILES_ACTIVE=mysql mvn spring-boot:run
```

## 常用命令

后端：

```bash
cd backend
mvn test
mvn spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
npm run build
npm run preview
```

## 开发说明

- 后端采用横向分层结构：`controller`、`service`、`repository`、`entity`、`dto`、`enums`。
- 普通用户接口位于 `controller` 包，管理端接口位于 `controller.admin` 包。
- 通用返回结构为 `ApiResponse<T>`，业务异常通过 `BusinessException` 和 `GlobalExceptionHandler` 统一处理。
- 登录成功后前端会把 Token 存入 `localStorage`，Axios 拦截器会自动携带 `Authorization` 请求头。
- 推荐系统会记录用户浏览、加购、下单行为，并根据行为权重计算商品相似度。

## 测试

后端测试使用 Spring Boot Test 和 H2 测试数据库：

```bash
cd backend
mvn test
```

当前测试覆盖了认证、商品、购物车、订单、管理员权限和推荐算法等核心流程。
