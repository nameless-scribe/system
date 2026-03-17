package com.system.user.service;

import com.system.user.entity.CartItem;

import java.util.List;

public interface CartService {

    List<CartItem> listByUserId(Long userId);

    void addItem(CartItem item);

    void updateItem(CartItem item);

    void removeItem(Long id);

    void clearByUserId(Long userId);
}

