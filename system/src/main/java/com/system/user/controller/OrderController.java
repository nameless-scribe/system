package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.Order;
import com.system.user.entity.OrderDetail;
import com.system.user.entity.User;
import com.system.user.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 创建订单：从购物车条目生成订单
     */
    @PostMapping
    public Result<Void> createOrder(@RequestBody CreateOrderRequest req,
                                    HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }

        if (req.getAddress() == null || req.getAddress().trim().isEmpty()) {
            return Result.fail("收货地址不能为空");
        }
        if (req.getAddress().length() > 255) {
            return Result.fail("收货地址长度不能超过 255 个字符");
        }

        Order order = new Order();
        order.setUserId(user.getId());
        order.setAddress(req.getAddress());
        // totalPrice 会在服务里按照商品价格重新计算，这里可不强制使用前端值
        orderService.createOrder(order, req.getCartItemIds());
        return Result.success();
    }

    /**
     * 当前用户订单列表（分页）
     */
    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }

        List<Order> orders = orderService.findOrderByUserId(user.getId(), page, size);
        int total = orderService.findOrderTotalCount(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("list", orders);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 我卖出的订单列表（分页）
     */
    @GetMapping("/sold")
    public Result<Map<String, Object>> soldList(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }

        List<Order> orders = orderService.findSoldOrderByOwnerId(user.getId(), page, size);
        int total = orderService.findSoldOrderTotalCount(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("list", orders);
        data.put("total", total);
        return Result.success(data);
    }

    /**
     * 订单详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable("id") Long orderId) {
        Order order = orderService.getOrderById(orderId);
        List<OrderDetail> details = orderService.getOrderDetails(orderId);
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("details", details);
        return Result.success(data);
    }

    /**
     * 买家：标记订单为已支付（当前业务已改为下单即待发货，本接口不再使用）
     */
    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable("id") Long orderId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }
        return Result.fail("当前订单无需支付，提交后即为待发货状态");
    }

    /**
     * 卖家：标记订单为已发货
     */
    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable("id") Long orderId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }
        try {
            orderService.shipOrder(orderId, user.getId());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 买家：确认收货，订单完成
     */
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable("id") Long orderId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }
        Order order = orderService.getOrderById(orderId);
        if (!user.getId().equals(order.getUserId())) {
            return Result.fail("无权操作该订单");
        }
        if (order.getOrderStatus() != 2) {
            return Result.fail("只有已发货订单可以完成");
        }
        orderService.updateStatus(orderId, 3);
        return Result.success();
    }

    /**
     * 买家：取消待支付订单（保留的旧业务旧逻辑，不再使用）
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long orderId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }
        Order order = orderService.getOrderById(orderId);
        if (!user.getId().equals(order.getUserId())) {
            return Result.fail("无权操作该订单");
        }
        if (order.getOrderStatus() != 0) {
            return Result.fail("只有待支付订单可以取消");
        }
        // 约定 4 表示已取消
        orderService.updateStatus(orderId, 4);
        return Result.success();
    }

    /**
     * 买家：申请退回（仅已发货订单）
     */
    @PutMapping("/{id}/return")
    public Result<Void> requestReturn(@PathVariable("id") Long orderId,
                                      @RequestBody ReturnRequest req,
                                      HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }
        try {
            orderService.requestReturn(orderId, user.getId(), req.getReason(), req.getPickupAddress());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 卖家：确认已取回退回的商品（已退回 -> 已取消）
     */
    @PutMapping("/{id}/pickup")
    public Result<Void> pickup(@PathVariable("id") Long orderId, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }
        try {
            orderService.pickupReturn(orderId, user.getId());
            return Result.success();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 创建订单请求 DTO
     */
    public static class CreateOrderRequest {
        private String address;
        private List<Long> cartItemIds;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public List<Long> getCartItemIds() {
            return cartItemIds;
        }

        public void setCartItemIds(List<Long> cartItemIds) {
            this.cartItemIds = cartItemIds;
        }
    }

    /**
     * 退回申请 DTO
     */
    public static class ReturnRequest {
        private String reason;
        private String pickupAddress;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getPickupAddress() {
            return pickupAddress;
        }

        public void setPickupAddress(String pickupAddress) {
            this.pickupAddress = pickupAddress;
        }
    }
}

