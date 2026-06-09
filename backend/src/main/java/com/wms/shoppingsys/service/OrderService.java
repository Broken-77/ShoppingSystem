package com.wms.shoppingsys.service;

import com.wms.shoppingsys.entity.Order;
import com.wms.shoppingsys.entity.OrderItem;
import com.wms.shoppingsys.enums.OrderStatus;
import com.wms.shoppingsys.repository.OrderItemRepository;
import com.wms.shoppingsys.repository.OrderRepository;

import com.wms.shoppingsys.entity.CartItem;
import com.wms.shoppingsys.repository.CartItemRepository;
import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.service.ProductService;
import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.common.ErrorCode;
import com.wms.shoppingsys.service.BehaviorRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrderService {
    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final BehaviorRecorder behaviorRecorder;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository, ProductService productService,
                        BehaviorRecorder behaviorRecorder) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.behaviorRecorder = behaviorRecorder;
    }

    @Transactional
    public Order createOrder(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "购物车为空");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            Product product = productService.getOnSaleProduct(item.getProductId());
            if (item.getQuantity() > product.getStock()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "库存不足");
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP));
        }

        Order order = orderRepository.save(new Order(generateOrderNo(), userId, total));
        for (CartItem item : cartItems) {
            Product product = productService.getOnSaleProduct(item.getProductId());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            product.deductStock(item.getQuantity());
            orderItemRepository.save(new OrderItem(order.getId(), product.getId(), product.getName(),
                    product.getPrice(), item.getQuantity(), subtotal));
            behaviorRecorder.recordOrder(userId, product.getId());
        }
        cartItemRepository.deleteByUserId(userId);
        return order;
    }

    @Transactional
    public Order createOrderFromSelected(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择至少一件商品");
        }

        List<CartItem> selectedItems = new java.util.ArrayList<>();
        for (Long itemId : cartItemIds) {
            CartItem item = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "购物车项不存在"));
            if (!userId.equals(item.getUserId())) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "购物车项不存在");
            }
            selectedItems.add(item);
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : selectedItems) {
            Product product = productService.getOnSaleProduct(item.getProductId());
            if (item.getQuantity() > product.getStock()) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "库存不足");
            }
            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP));
        }

        Order order = orderRepository.save(new Order(generateOrderNo(), userId, total));
        for (CartItem item : selectedItems) {
            Product product = productService.getOnSaleProduct(item.getProductId());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            product.deductStock(item.getQuantity());
            orderItemRepository.save(new OrderItem(order.getId(), product.getId(), product.getName(),
                    product.getPrice(), item.getQuantity(), subtotal));
            behaviorRecorder.recordOrder(userId, product.getId());
        }
        for (CartItem item : selectedItems) {
            cartItemRepository.delete(item);
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> listUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Order> listAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "订单不存在"));
    }

    @Transactional(readOnly = true)
    public Order getOwnedOrder(Long userId, Long id) {
        Order order = getOrder(id);
        if (!userId.equals(order.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional
    public Order pay(Long userId, Long id) {
        Order order = getOwnedOrder(userId, id);
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATE, "订单状态不允许该操作");
        }
        order.pay();
        return order;
    }

    @Transactional
    public Order updateStatus(Long id, OrderStatus status) {
        Order order = getOrder(id);
        boolean allowed = (order.getStatus() == OrderStatus.PAID && status == OrderStatus.FINISHED)
                || (order.getStatus() == OrderStatus.PENDING_PAYMENT && status == OrderStatus.CANCELLED);
        if (!allowed) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_STATE, "订单状态不允许该操作");
        }
        order.changeStatus(status);
        return order;
    }

    private String generateOrderNo() {
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "SO" + ORDER_NO_FORMAT.format(LocalDateTime.now()) + random;
    }
}
