package com.system.admin.controller;

import com.system.admin.entity.Goods;
import com.system.admin.service.GoodsService;
import com.system.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping
    public Result<List<Goods>> list() {
        return Result.success(goodsService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Goods> detail(@PathVariable Long id) {
        return Result.success(goodsService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Goods goods) {
        goodsService.create(goods);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Goods goods) {
        goods.setId(id);
        goodsService.update(goods);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        goodsService.delete(id);
        return Result.success();
    }

    /**
     * 审核通过：将商品审核状态置为已通过，清空退回原因
     */
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        goods.setAuditStatus(1);
        goods.setAuditRemark(null);
        // 审核通过但不上架，由发布者自行决定何时上架
        goodsService.update(goods);
        return Result.success();
    }

    /**
     * 审核退回：管理员填写退回原因，状态置为被退回，下架该商品
     */
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody RejectRequest req) {
        Goods goods = goodsService.getById(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        goods.setAuditStatus(2);
        goods.setAuditRemark(req.getReason());
        goods.setStatus(0);
        goodsService.update(goods);
        return Result.success();
    }

    public static class RejectRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}

