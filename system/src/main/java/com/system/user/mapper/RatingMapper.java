package com.system.user.mapper;

import com.system.user.entity.GoodsCommentView;
import com.system.user.entity.Rating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RatingMapper {

    void insert(Rating rating);

    List<Rating> selectByToUserId(@Param("toUserId") Long toUserId);

    Rating selectByOrderAndFromUser(@Param("orderId") Long orderId,
                                    @Param("fromUserId") Long fromUserId);

    /**
     * 查询某商品的历史成交评论（通过订单明细关联）。
     */
    List<GoodsCommentView> selectCommentsByGoodsId(@Param("goodsId") Long goodsId,
                                                   @Param("size") int size);

    void updateById(@Param("id") Long id,
                    @Param("score") Integer score,
                    @Param("comment") String comment);

    List<Rating> selectByFromUserId(@Param("fromUserId") Long fromUserId);

    void deleteByIdAndFromUser(@Param("id") Long id, @Param("fromUserId") Long fromUserId);
}

