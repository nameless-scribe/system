package com.system.user.entity;

import lombok.Data;

/**
 * 购物车条目（简化版）
 */
@Data
public class CartItem {

    private Long id;
    private Long userId;
    private Long goodsId;
    private Integer quantity;
}

