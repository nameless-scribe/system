package com.system.admin.service;

import com.system.admin.entity.Goods;

import java.util.List;

public interface GoodsService {

    Goods getById(Long id);

    List<Goods> listAll();

    /**
     * 前台商品查询，支持多条件筛选
     */
    List<Goods> queryForShop(Long brandId, Double minPrice, Double maxPrice,
                             Integer conditionLevel, String sort);

    void create(Goods goods);

    void update(Goods goods);

    void delete(Long id);

    /**
     * 浏览详情时增加浏览量
     */
    void increaseViewCount(Long id);

    /**
     * 前台：卖家在售商品（上架+审核通过+未删除）
     */
    List<Goods> listOnSaleByOwnerId(Long ownerId, int size);

    int countOnSaleByOwnerId(Long ownerId);
}

