package com.system.user.entity;

import lombok.Data;

import java.util.Date;

/**
 * 订单主表（简化版，对齐 demo 的字段设计）
 */
@Data
public class Order {

    private Long id;
    private Long userId;
    private Long orderNum;
    private Double totalPrice;
    private Integer orderStatus;
    private String address;
    private Boolean deleteStatus;
    private Date addTime;
    /** 发货时间（用于自动确认收货） */
    private Date shipTime;
    /** 完成时间 */
    private Date completeTime;
    /** 退回原因 */
    private String returnReason;
    /** 退回取货地址 */
    private String returnAddress;
}

