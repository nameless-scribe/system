package com.system.user.controller;

import com.system.admin.entity.Goods;
import com.system.admin.service.GoodsService;
import com.system.common.Result;
import com.system.user.entity.GoodsCommentView;
import com.system.user.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 前台商品浏览接口（只返回上架商品）
 */
@RestController
@RequestMapping("/api/shop/goods")
public class ShopGoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RatingService ratingService;

    /**
     * 商品列表：支持按分类（brandId）、价格区间、新旧程度、排序筛选
     */
    @GetMapping
    public Result<List<Goods>> list(@RequestParam(value = "brandId", required = false) Long brandId,
                                    @RequestParam(value = "minPrice", required = false) Double minPrice,
                                    @RequestParam(value = "maxPrice", required = false) Double maxPrice,
                                    @RequestParam(value = "conditionLevel", required = false) Integer conditionLevel,
                                    @RequestParam(value = "sort", required = false) String sort) {
        List<Goods> result = goodsService.queryForShop(brandId, minPrice, maxPrice, conditionLevel, sort);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<Goods> detail(@PathVariable Long id) {
        Goods goods = goodsService.getById(id);
        if (goods == null || goods.getStatus() == null || goods.getStatus() == 0) {
            return Result.fail("商品不存在或已下架");
        }
        // 浏览详情时增加浏览量
        goodsService.increaseViewCount(id);
        return Result.success(goods);
    }

    /**
     * 商品历史成交评论（用于详情页展示）
     */
    @GetMapping("/{id}/comments")
    public Result<List<GoodsCommentView>> comments(@PathVariable Long id,
                                                   @RequestParam(defaultValue = "10") int size) {
        return Result.success(ratingService.listGoodsComments(id, size));
    }
}

