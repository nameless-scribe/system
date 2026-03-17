package com.system.user.mapper;

import com.system.user.entity.FavoriteView;
import com.system.user.entity.UserFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFavoriteMapper {
    int insert(UserFavorite favorite);

    int delete(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    int countByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);

    List<FavoriteView> selectByUserId(@Param("userId") Long userId);

    int countByUserId(@Param("userId") Long userId);

    int deleteBatch(@Param("userId") Long userId, @Param("goodsIds") List<Long> goodsIds);

    int deleteInvalidByUserId(@Param("userId") Long userId);
}

