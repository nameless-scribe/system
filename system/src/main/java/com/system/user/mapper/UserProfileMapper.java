package com.system.user.mapper;

import com.system.user.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {

    UserProfile selectByUserId(@Param("userId") Long userId);

    int insert(UserProfile profile);

    int updateByUserId(UserProfile profile);
}

