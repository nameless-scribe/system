package com.system.admin.controller;

import com.system.common.Result;
import com.system.user.service.BuyRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端：求购信息管理
 */
@RestController
@RequestMapping("/api/admin/buyRequests")
public class AdminBuyRequestController {

    @Autowired
    private BuyRequestService buyRequestService;

    @GetMapping
    public Result<Map<String, Object>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.success(buyRequestService.adminList(keyword, status, page, size));
    }

    /**
     * 删除（软删除）：status=0
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        buyRequestService.adminDelete(id);
        return Result.success();
    }
}

