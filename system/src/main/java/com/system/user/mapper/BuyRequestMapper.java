package com.system.user.mapper;

import com.system.user.entity.BuyRequest;
import com.system.user.entity.BuyRequestView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BuyRequestMapper {
    int insert(BuyRequest req);

    BuyRequest selectById(@Param("id") Long id);

    BuyRequestView selectViewById(@Param("id") Long id);

    /**
     * 置顶 TopN：按点赞数、评论数综合排序
     */
    List<BuyRequestView> selectPinnedTop(@Param("top") int top);

    /**
     * 非置顶列表：按时间倒序
     */
    List<BuyRequestView> selectLatestExcludeIds(@Param("excludeIds") List<Long> excludeIds,
                                                @Param("start") int start,
                                                @Param("size") int size);

    int countLatestExcludeIds(@Param("excludeIds") List<Long> excludeIds);

    int incLikeCount(@Param("id") Long id, @Param("delta") int delta);

    int incCommentCount(@Param("id") Long id, @Param("delta") int delta);

    List<BuyRequestView> selectAdminList(@Param("keyword") String keyword,
                                         @Param("status") Integer status,
                                         @Param("start") int start,
                                         @Param("size") int size);

    int countAdminList(@Param("keyword") String keyword, @Param("status") Integer status);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

