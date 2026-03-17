package com.system.user.service;

import com.system.user.entity.Order;
import com.system.user.entity.OrderDetail;

import java.util.List;

public interface OrderService {

    void createOrder(Order order, List<Long> cartItemIds);

    List<Order> findOrderByUserId(Long userId, int page, int size);

    int findOrderTotalCount(Long userId);

    /**
     * 我卖出的订单（根据商品发布人统计）
     */
    List<Order> findSoldOrderByOwnerId(Long ownerId, int page, int size);

    int findSoldOrderTotalCount(Long ownerId);

    Order getOrderById(Long orderId);

    List<OrderDetail> getOrderDetails(Long orderId);

    /**
     * 管理端：分页查询全部订单，可按状态过滤
     */
    List<Order> findAll(Integer status, int page, int size);

    int findAllTotalCount(Integer status);

    void updateStatus(Long orderId, Integer status);

    /**
     * 买家支付订单：校验权限、状态并扣减对应商品库存（当前业务已改为下单即待发货，可视需要保留或废弃）
     */
    void payOrder(Long orderId, Long userId);

    /**
     * 当前用户是否为该订单的卖家（用于发货权限判断）
     */
    boolean isSellerOfOrder(Long orderId, Long ownerId);

    /**
     * 卖家发货：将订单状态改为已发货，并记录发货时间
     */
    void shipOrder(Long orderId, Long sellerId);

    /**
     * 买家申请退回：仅已发货订单可退回
     */
    void requestReturn(Long orderId, Long buyerId, String reason, String address);

    /**
     * 卖家确认已取回退货：已退回 -> 已取消
     */
    void pickupReturn(Long orderId, Long sellerId);

    /**
     * 定时任务：自动将发货超过 N 天未确认收货的订单置为已完成
     */
    void autoCompleteShippedOrders(int days);
}

