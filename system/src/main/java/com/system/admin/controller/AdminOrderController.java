package com.system.admin.controller;

import com.system.common.Result;
import com.system.user.entity.Order;
import com.system.user.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端订单管理
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Integer status) {
        List<Order> orders = orderService.findAll(status, page, size);
        int total = orderService.findAllTotalCount(status);
        Map<String, Object> data = new HashMap<>();
        data.put("list", orders);
        data.put("total", total);
        return Result.success(data);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        orderService.updateStatus(id, status);
        return Result.success();
    }
}

