package com.system.user.mapper;

import com.system.user.entity.UserReputation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserReputationMapper {

    UserReputation selectByUserId(@Param("userId") Long userId);

    int insert(UserReputation rep);

    int update(UserReputation rep);
}

