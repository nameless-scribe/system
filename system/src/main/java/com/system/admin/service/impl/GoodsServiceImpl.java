package com.system.admin.service.impl;

import com.system.admin.entity.Goods;
import com.system.admin.mapper.GoodsMapper;
import com.system.admin.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public Goods getById(Long id) {
        return goodsMapper.selectById(id);
    }

    /**
     * 浏览详情时增加浏览量
     */
    public void increaseViewCount(Long id) {
        if (id == null) {
            return;
        }
        goodsMapper.increaseViewCount(id);
    }

    @Override
    public List<Goods> listAll() {
        return goodsMapper.selectAll();
    }

    @Override
    public List<Goods> queryForShop(Long brandId, Double minPrice, Double maxPrice,
                                    Integer conditionLevel, String sort) {
        return goodsMapper.selectForShop(brandId, minPrice, maxPrice, conditionLevel, sort);
    }

    @Override
    public void create(Goods goods) {
        validateGoods(goods);
        // 新增商品时，浏览量默认为 0，避免 view_count 为空导致数据库约束错误
        if (goods.getViewCount() == null) {
            goods.setViewCount(0);
        }
        if (goods.getDeleteStatus() == null) {
            goods.setDeleteStatus(0);
        }
        goodsMapper.insert(goods);
    }

    @Override
    public void update(Goods goods) {
        validateGoods(goods);
        goodsMapper.update(goods);
    }

    @Override
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("商品ID不合法");
        }
        Goods goods = goodsMapper.selectByIdIncludeDeleted(id);
        if (goods == null) {
            throw new IllegalArgumentException("商品不存在或已删除");
        }
        if (goods.getDeleteStatus() != null && goods.getDeleteStatus() == 1) {
            throw new IllegalArgumentException("商品已删除，请勿重复操作");
        }
        int updated = goodsMapper.softDeleteById(id);
        if (updated <= 0) {
            throw new IllegalArgumentException("删除失败，请稍后重试");
        }
    }

    @Override
    public List<Goods> listOnSaleByOwnerId(Long ownerId, int size) {
        if (ownerId == null) {
            return List.of();
        }
        int s = size <= 0 ? 6 : Math.min(size, 20);
        return goodsMapper.selectOnSaleByOwnerId(ownerId, s);
    }

    @Override
    public int countOnSaleByOwnerId(Long ownerId) {
        if (ownerId == null) {
            return 0;
        }
        return goodsMapper.countOnSaleByOwnerId(ownerId);
    }

    /**
     * 商品基础校验：名称、价格、库存、分类等
     */
    private void validateGoods(Goods goods) {
        if (goods == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        if (goods.getName() == null || goods.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("商品名称不能为空");
        }
        if (goods.getName().length() > 100) {
            throw new IllegalArgumentException("商品名称长度不能超过 100 个字符");
        }
        if (goods.getBrandId() == null) {
            throw new IllegalArgumentException("商品分类不能为空");
        }
        if (goods.getPrice() == null || goods.getPrice() < 0) {
            throw new IllegalArgumentException("价格必须大于或等于 0");
        }
        if (goods.getStock() == null || goods.getStock() < 0) {
            throw new IllegalArgumentException("库存不能为负数");
        }
        if (goods.getDescription() != null && goods.getDescription().length() > 1000) {
            throw new IllegalArgumentException("商品描述长度不能超过 1000 个字符");
        }
    }
}

