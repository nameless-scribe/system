package com.system.user.mapper;

import com.system.user.entity.BuyRequestComment;
import com.system.user.entity.BuyRequestCommentView;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BuyRequestCommentMapper {
    int insert(BuyRequestComment c);

    List<BuyRequestCommentView> selectByRequestId(@Param("requestId") Long requestId);
}

