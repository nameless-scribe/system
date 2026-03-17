package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.BuyRequestCommentView;
import com.system.user.entity.BuyRequestView;
import com.system.user.service.BuyRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 前台：求购广场（可浏览）
 */
@RestController
@RequestMapping("/api/shop/buyRequests")
public class ShopBuyRequestController {

    @Autowired
    private BuyRequestService buyRequestService;

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(buyRequestService.listSquare(page, size));
    }

    @GetMapping("/{id}")
    public Result<BuyRequestView> detail(@PathVariable Long id) {
        BuyRequestView v = buyRequestService.detail(id);
        if (v == null) {
            return Result.fail("求购信息不存在或已下架");
        }
        return Result.success(v);
    }

    @GetMapping("/{id}/comments")
    public Result<List<BuyRequestCommentView>> comments(@PathVariable Long id) {
        return Result.success(buyRequestService.comments(id));
    }
}

