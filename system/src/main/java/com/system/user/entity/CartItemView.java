package com.system.user.entity;

import lombok.Data;

/**
 * 购物车前端展示视图对象：包含商品基础信息与当前库存
 */
@Data
public class CartItemView {

    private Long id;
    private Long goodsId;
    private Integer quantity;

    private String goodsName;
    private String brandName;
    private Double price;
    private Integer stock;
}

