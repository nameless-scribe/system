package com.system.admin.controller;

import com.system.common.Result;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单统计接口：用户数、订单数、今日订单数、按品牌成交金额
 */
@RestController
@RequestMapping("/api/admin/stats")
public class StatsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new HashMap<>();

        Long userCount = jdbcTemplate.queryForObject("select count(*) from user", Long.class);
        Long orderCount = jdbcTemplate.queryForObject("select count(*) from orders where delete_status = 0", Long.class);
        Long todayOrderCount = jdbcTemplate.queryForObject(
                "select count(*) from orders where delete_status = 0 and date(add_time) = current_date",
                Long.class);

        data.put("userCount", userCount);
        data.put("orderCount", orderCount);
        data.put("todayOrderCount", todayOrderCount);

        List<Map<String, Object>> brandSales = jdbcTemplate.queryForList(
                "select b.name as brandName, ifnull(sum(d.count * d.price),0) as totalAmount " +
                        "from brand b " +
                        "left join goods g on g.brand_id = b.id " +
                        "left join order_detail d on d.goods_id = g.id " +
                        "left join orders o on o.id = d.order_id and o.delete_status = 0 " +
                        "group by b.id, b.name " +
                        "order by totalAmount desc " +
                        "limit 10");
        data.put("brandSales", brandSales);

        return Result.success(data);
    }
}

