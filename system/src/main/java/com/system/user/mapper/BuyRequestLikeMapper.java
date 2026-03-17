package com.system.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BuyRequestLikeMapper {
    int insert(@Param("requestId") Long requestId, @Param("userId") Long userId);

    int delete(@Param("requestId") Long requestId, @Param("userId") Long userId);

    int countByReqAndUser(@Param("requestId") Long requestId, @Param("userId") Long userId);
}

