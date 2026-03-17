package com.system.admin.mapper;

import com.system.admin.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper {

    Goods selectById(@Param("id") Long id);

    List<Goods> selectAll();

    List<Goods> selectForShop(@Param("brandId") Long brandId,
                              @Param("minPrice") Double minPrice,
                              @Param("maxPrice") Double maxPrice,
                              @Param("conditionLevel") Integer conditionLevel,
                              @Param("sort") String sort);

    int insert(Goods goods);

    int update(Goods goods);

    /**
     * 支付订单时扣减库存：stock = stock - count，并要求当前库存充足
     */
    int decreaseStock(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 退回订单确认取货后回滚库存：stock = stock + count
     */
    int increaseStock(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 浏览详情时增加浏览量
     */
    int increaseViewCount(@Param("id") Long id);

    /**
     * 逻辑删除商品：delete_status = 1（并同步下架）
     */
    int softDeleteById(@Param("id") Long id);

    /**
     * 仅供管理端按需查询：包含已删除
     */
    Goods selectByIdIncludeDeleted(@Param("id") Long id);

    /**
     * 前台：查询某卖家在售商品（上架+审核通过+未删除）
     */
    List<Goods> selectOnSaleByOwnerId(@Param("ownerId") Long ownerId, @Param("size") int size);

    int countOnSaleByOwnerId(@Param("ownerId") Long ownerId);
}

