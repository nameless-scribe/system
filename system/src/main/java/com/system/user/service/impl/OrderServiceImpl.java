package com.system.user.service.impl;

import com.system.admin.entity.Goods;
import com.system.admin.mapper.GoodsMapper;
import com.system.user.entity.CartItem;
import com.system.user.entity.Order;
import com.system.user.entity.OrderDetail;
import com.system.user.mapper.CartMapper;
import com.system.user.mapper.OrderDetailMapper;
import com.system.user.mapper.OrderMapper;
import com.system.user.service.OrderService;
import com.system.util.SnowflakeIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Transactional
    @Override
    public void createOrder(Order order, List<Long> cartItemIds) {
        if (CollectionUtils.isEmpty(cartItemIds)) {
            throw new IllegalArgumentException("请选择要结算的购物车商品");
        }

        // 查询购物车明细
        List<CartItem> cartItems = cartMapper.selectByIds(cartItemIds);
        if (CollectionUtils.isEmpty(cartItems)) {
            throw new IllegalArgumentException("购物车项不存在");
        }

        List<OrderDetail> details = new ArrayList<>();
        double totalPrice = 0D;
        for (CartItem cart : cartItems) {
            Goods goods = goodsMapper.selectById(cart.getGoodsId());
            if (goods == null) {
                throw new IllegalArgumentException("商品不存在，id=" + cart.getGoodsId());
            }
            if (goods.getOwnerId() != null && goods.getOwnerId().equals(order.getUserId())) {
                throw new IllegalArgumentException("不能购买自己发布的商品");
            }

            // 创建订单时先做一次库存校验，给出及时反馈（最终以支付时扣减为准）
            Integer stock = goods.getStock();
            int need = cart.getQuantity() == null ? 0 : cart.getQuantity();
            if (need <= 0) {
                continue;
            }
            if (stock == null || stock < need) {
                throw new IllegalArgumentException("商品【" + goods.getName() + "】库存不足");
            }

            OrderDetail detail = new OrderDetail();
            detail.setGoodsId(cart.getGoodsId());
            detail.setCount(cart.getQuantity());
            double price = goods.getPrice() != null ? goods.getPrice() : 0D;
            detail.setPrice(price);
            details.add(detail);

            totalPrice += price * cart.getQuantity();
        }

        // 设置订单基础信息和总金额
        order.setOrderNum(SnowflakeIdGenerator.generateId());
        // 业务调整：下单即视为待发货（已付款），后续仅处理发货/收货/退回
        order.setOrderStatus(1); // 1-待发货
        order.setDeleteStatus(false);
        order.setAddTime(new Date());
        order.setTotalPrice(totalPrice);

        // 保存订单主表
        orderMapper.insert(order);
        Long orderId = order.getId();
        if (orderId == null) {
            throw new IllegalArgumentException("订单主表ID回填失败");
        }

        // 回填订单ID到明细
        for (OrderDetail detail : details) {
            detail.setOrderId(orderId);
        }

        orderDetailMapper.batchInsert(details);

        // 创建订单成功后立即扣减库存（代替原先在支付时扣减）
        for (OrderDetail detail : details) {
            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            if (goods == null) {
                throw new IllegalArgumentException("商品不存在，id=" + detail.getGoodsId());
            }
            Integer stock = goods.getStock();
            int need = detail.getCount() == null ? 0 : detail.getCount();
            if (need <= 0) {
                continue;
            }
            if (stock == null || stock < need) {
                throw new IllegalArgumentException("商品【" + goods.getName() + "】库存不足");
            }
            int updated = goodsMapper.decreaseStock(goods.getId(), need);
            if (updated <= 0) {
                throw new IllegalArgumentException("商品【" + goods.getName() + "】库存不足，请稍后重试");
            }
        }

        // 删除购物车中的条目
        for (Long id : cartItemIds) {
            cartMapper.deleteById(id);
        }
    }

    @Override
    public List<Order> findOrderByUserId(Long userId, int page, int size) {
        int start = (page - 1) * size;
        return orderMapper.selectByUserId(userId, start, size);
    }

    @Override
    public int findOrderTotalCount(Long userId) {
        return orderMapper.selectTotalCountByUserId(userId);
    }

    @Override
    public List<Order> findSoldOrderByOwnerId(Long ownerId, int page, int size) {
        int start = (page - 1) * size;
        return orderMapper.selectSoldByOwnerId(ownerId, start, size);
    }

    @Override
    public int findSoldOrderTotalCount(Long ownerId) {
        return orderMapper.selectSoldTotalCountByOwnerId(ownerId);
    }

    @Override
    public Order getOrderById(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("订单ID不合法：" + orderId);
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在或已删除");
        }
        return order;
    }

    @Override
    public List<OrderDetail> getOrderDetails(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("订单ID不合法：" + orderId);
        }
        return orderDetailMapper.getByOrderId(orderId);
    }

    @Override
    public List<Order> findAll(Integer status, int page, int size) {
        int start = (page - 1) * size;
        return orderMapper.selectAll(status, start, size);
    }

    @Override
    public int findAllTotalCount(Integer status) {
        return orderMapper.selectTotalCount(status);
    }

    @Override
    public void updateStatus(Long orderId, Integer status) {
        orderMapper.updateStatus(orderId, status);
    }

    /**
     * 买家支付订单：校验订单归属与状态，并扣减对应商品库存
     */
    @Transactional
    @Override
    public void payOrder(Long orderId, Long userId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("订单ID不合法：" + orderId);
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不合法：" + userId);
        }

        // 1. 查询订单并校验归属与状态
        Order order = orderMapper.selectById(orderId);
        if (order == null || Boolean.TRUE.equals(order.getDeleteStatus())) {
            throw new IllegalArgumentException("订单不存在或已删除");
        }
        if (!userId.equals(order.getUserId())) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 0) {
            throw new IllegalArgumentException("只有待支付订单可以支付");
        }

        // 2. 查询订单明细
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orderId);
        if (CollectionUtils.isEmpty(details)) {
            throw new IllegalArgumentException("订单明细不存在");
        }

        // 3. 逐条扣减库存（先检查、再扣减，防止库存不足）
        for (OrderDetail detail : details) {
            Goods goods = goodsMapper.selectById(detail.getGoodsId());
            if (goods == null) {
                throw new IllegalArgumentException("商品不存在，id=" + detail.getGoodsId());
            }
            Integer stock = goods.getStock();
            int need = detail.getCount() == null ? 0 : detail.getCount();
            if (need <= 0) {
                continue;
            }
            if (stock == null || stock < need) {
                throw new IllegalArgumentException("商品【" + goods.getName() + "】库存不足");
            }
            int updated = goodsMapper.decreaseStock(goods.getId(), need);
            if (updated <= 0) {
                throw new IllegalArgumentException("商品【" + goods.getName() + "】库存不足，请稍后重试");
            }
        }

        // 4. 扣减成功后，将订单状态改为已支付（1）
        orderMapper.updateStatus(orderId, 1);
    }

    @Override
    public boolean isSellerOfOrder(Long orderId, Long ownerId) {
        int cnt = orderMapper.countByOrderAndOwner(orderId, ownerId);
        return cnt > 0;
    }

    @Override
    public void shipOrder(Long orderId, Long sellerId) {
        Order order = getOrderById(orderId);
        if (order.getOrderStatus() == null || order.getOrderStatus() != 1) {
            throw new IllegalArgumentException("只有待发货订单可以发货");
        }
        if (!isSellerOfOrder(orderId, sellerId)) {
            throw new IllegalArgumentException("只有卖家可以发货");
        }
        orderMapper.updateStatusAndShipTime(orderId, 2, new Date()); // 2-已发货/待收货
    }

    @Override
    public void requestReturn(Long orderId, Long buyerId, String reason, String address) {
        Order order = getOrderById(orderId);
        if (!buyerId.equals(order.getUserId())) {
            throw new IllegalArgumentException("无权操作该订单");
        }
        if (order.getOrderStatus() == null || order.getOrderStatus() != 2) {
            throw new IllegalArgumentException("只有已发货订单可以申请退回");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("退回原因不能为空");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("取货地址不能为空");
        }
        orderMapper.updateReturnInfo(orderId, 5, reason, address); // 5-已退回
    }

    @Override
    @Transactional
    public void pickupReturn(Long orderId, Long sellerId) {
        Order order = getOrderById(orderId);
        if (order.getOrderStatus() == null || order.getOrderStatus() != 5) {
            throw new IllegalArgumentException("只有已退回订单可以确认取货");
        }
        if (!isSellerOfOrder(orderId, sellerId)) {
            throw new IllegalArgumentException("只有卖家可以确认取货");
        }
        // 卖家确认已取货后，将订单视为已取消，并回滚对应商品库存
        List<OrderDetail> details = orderDetailMapper.getByOrderId(orderId);
        if (!CollectionUtils.isEmpty(details)) {
            for (OrderDetail detail : details) {
                int count = detail.getCount() == null ? 0 : detail.getCount();
                if (count <= 0) {
                    continue;
                }
                goodsMapper.increaseStock(detail.getGoodsId(), count);
            }
        }
        orderMapper.updateStatus(orderId, 4); // 4-已取消
    }

    @Override
    public void autoCompleteShippedOrders(int days) {
        List<Long> ids = orderMapper.selectIdsToAutoComplete();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        Date now = new Date();
        for (Long id : ids) {
            orderMapper.updateStatusAndCompleteTime(id, 3, now); // 3-已完成
        }
    }
}

