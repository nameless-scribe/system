package com.system.user.mapper;

import com.system.user.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper {

    List<CartItem> selectByUserId(@Param("userId") Long userId);

    int insert(CartItem item);

    int update(CartItem item);

    int deleteById(@Param("id") Long id);

    int deleteByUserId(@Param("userId") Long userId);

    List<CartItem> selectByIds(@Param("ids") List<Long> ids);

    CartItem selectByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    int increaseQuantity(@Param("id") Long id, @Param("delta") Integer delta);
}

