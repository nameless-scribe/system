package com.system.user.service;

import com.system.user.entity.UserProfile;

public interface UserProfileService {

    UserProfile getByUserId(Long userId);

    void saveOrUpdate(UserProfile profile);
}

