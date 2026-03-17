package com.system.user.controller;

import com.system.admin.entity.Goods;
import com.system.admin.service.GoodsService;
import com.system.common.Result;
import com.system.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端：我发布的闲置物品管理
 */
@RestController
@RequestMapping("/api/user/items")
public class UserGoodsController {

    @Autowired
    private GoodsService goodsService;

    private Long currentUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null ? user.getId() : null;
    }

    @GetMapping
    public Result<List<Goods>> myItems(HttpServletRequest request) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.fail("未登录");
        }
        // 简化实现：先取全部再在内存中过滤 ownerId
        List<Goods> all = goodsService.listAll();
        all.removeIf(g -> g.getOwnerId() == null || !g.getOwnerId().equals(userId));
        return Result.success(all);
    }

    @PostMapping
    public Result<Void> create(HttpServletRequest request, @RequestBody Goods goods) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.fail("未登录");
        }
        goods.setOwnerId(userId);
        // 普通用户新发布的闲置：默认下架且待审核
        goods.setStatus(0);
        goods.setAuditStatus(0);
        goods.setAuditRemark(null);
        goodsService.create(goods);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Goods goods) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.fail("未登录");
        }
        goods.setId(id);
        goods.setOwnerId(userId);
        // 如果之前被退回，用户修改后重新发起审核：重置为待审核、下架状态
        goods.setAuditStatus(0);
        goods.setAuditRemark(null);
        if (goods.getStatus() == null || goods.getStatus() == 1) {
            goods.setStatus(0);
        }
        goodsService.update(goods);
        return Result.success();
    }

    @PutMapping("/{id}/off")
    public Result<Void> off(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.fail("未登录");
        }
        Goods goods = goodsService.getById(id);
        if (goods == null || goods.getOwnerId() == null || !goods.getOwnerId().equals(userId)) {
            return Result.fail("无权操作该物品");
        }
        goods.setStatus(0);
        goodsService.update(goods);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.fail("未登录");
        }
        Goods goods = goodsService.getById(id);
        if (goods == null || goods.getOwnerId() == null || !goods.getOwnerId().equals(userId)) {
            return Result.fail("无权操作该物品");
        }
        goodsService.delete(id);
        return Result.success();
    }

    /**
     * 上架：仅审核通过的商品才能上架，状态改为交易中
     */
    @PutMapping("/{id}/on")
    public Result<Void> on(HttpServletRequest request, @PathVariable Long id) {
        Long userId = currentUserId(request);
        if (userId == null) {
            return Result.fail("未登录");
        }
        Goods goods = goodsService.getById(id);
        if (goods == null || goods.getOwnerId() == null || !goods.getOwnerId().equals(userId)) {
            return Result.fail("无权操作该物品");
        }
        if (goods.getAuditStatus() == null || goods.getAuditStatus() != 1) {
            return Result.fail("商品尚未审核通过，无法上架");
        }
        goods.setStatus(1);
        goodsService.update(goods);
        return Result.success();
    }
}

