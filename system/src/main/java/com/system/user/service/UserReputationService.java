package com.system.user.service;

import com.system.user.entity.Rating;
import com.system.user.entity.UserReputation;

public interface UserReputationService {

    /**
     * 新增一条评价后，增量更新被评价用户的信誉
     */
    void updateOnNewRating(Rating rating);

    UserReputation getByUserId(Long userId);
}

