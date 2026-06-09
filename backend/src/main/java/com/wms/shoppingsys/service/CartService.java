package com.wms.shoppingsys.service;

import com.wms.shoppingsys.dto.CartDtos;
import com.wms.shoppingsys.entity.CartItem;
import com.wms.shoppingsys.repository.CartItemRepository;

import com.wms.shoppingsys.entity.Product;
import com.wms.shoppingsys.service.ProductService;
import com.wms.shoppingsys.common.BusinessException;
import com.wms.shoppingsys.common.ErrorCode;
import com.wms.shoppingsys.service.BehaviorRecorder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    private final BehaviorRecorder behaviorRecorder;

    public CartService(CartItemRepository cartItemRepository, ProductService productService,
                       BehaviorRecorder behaviorRecorder) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
        this.behaviorRecorder = behaviorRecorder;
    }

    @Transactional(readOnly = true)
    public List<CartItem> list(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public CartItem add(Long userId, CartDtos.AddCartItemRequest request) {
        Product product = productService.getOnSaleProduct(request.productId());
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, request.productId())
                .orElseGet(() -> new CartItem(userId, request.productId(), 0));
        int nextQuantity = item.getQuantity() + request.quantity();
        ensureStock(product, nextQuantity);
        item.changeQuantity(nextQuantity);
        CartItem saved = cartItemRepository.save(item);
        behaviorRecorder.recordCart(userId, product.getId());
        return saved;
    }

    @Transactional
    public CartItem update(Long userId, Long itemId, CartDtos.UpdateCartItemRequest request) {
        CartItem item = getOwnedItem(userId, itemId);
        Product product = productService.getOnSaleProduct(item.getProductId());
        ensureStock(product, request.quantity());
        item.changeQuantity(request.quantity());
        return item;
    }

    @Transactional
    public void delete(Long userId, Long itemId) {
        cartItemRepository.delete(getOwnedItem(userId, itemId));
    }

    @Transactional
    public void clear(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItem getOwnedItem(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "购物车项不存在"));
        if (!userId.equals(item.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车项不存在");
        }
        return item;
    }

    private void ensureStock(Product product, int quantity) {
        if (quantity > product.getStock()) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "库存不足");
        }
    }
}
