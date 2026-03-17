package com.system.user.service.impl;

import com.system.user.entity.CartItem;
import com.system.user.mapper.CartMapper;
import com.system.user.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartMapper cartMapper;

    @Override
    public List<CartItem> listByUserId(Long userId) {
        return cartMapper.selectByUserId(userId);
    }

    @Override
    public void addItem(CartItem item) {
        // 如果同一用户的购物车中已经有该商品，则在原有数量上累加
        CartItem exists = cartMapper.selectByUserAndGoods(item.getUserId(), item.getGoodsId());
        if (exists != null) {
            int delta = item.getQuantity() == null ? 0 : item.getQuantity();
            if (delta > 0) {
                cartMapper.increaseQuantity(exists.getId(), delta);
            }
        } else {
            cartMapper.insert(item);
        }
    }

    @Override
    public void updateItem(CartItem item) {
        cartMapper.update(item);
    }

    @Override
    public void removeItem(Long id) {
        cartMapper.deleteById(id);
    }

    @Override
    public void clearByUserId(Long userId) {
        cartMapper.deleteByUserId(userId);
    }
}

