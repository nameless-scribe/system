package com.system.user.entity;

import lombok.Data;

/**
 * 订单明细（简化版）
 */
@Data
public class OrderDetail {

    private Long id;
    private Long orderId;
    private Long goodsId;
    private Integer count;
    private Double price;
    private String goodsName;
    private String goodsPath;
}

