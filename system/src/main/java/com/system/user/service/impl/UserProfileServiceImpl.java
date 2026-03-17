package com.system.user.service.impl;

import com.system.user.entity.UserProfile;
import com.system.user.mapper.UserProfileMapper;
import com.system.user.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Override
    public UserProfile getByUserId(Long userId) {
        return userProfileMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public void saveOrUpdate(UserProfile profile) {
        if (profile.getUserId() == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        UserProfile exists = userProfileMapper.selectByUserId(profile.getUserId());
        if (exists == null) {
            userProfileMapper.insert(profile);
        } else {
            userProfileMapper.updateByUserId(profile);
        }
    }
}

