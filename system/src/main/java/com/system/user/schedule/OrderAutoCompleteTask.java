package com.system.user.schedule;

import com.system.user.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务：自动将已发货超过 7 天的订单标记为已完成
 */
@Component
public class OrderAutoCompleteTask {

    @Autowired
    private OrderService orderService;

    /**
     * 每天凌晨 3 点执行一次
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoComplete() {
        // 以发货时间为准超过 7 天自动确认收货
        orderService.autoCompleteShippedOrders(7);
    }
}

