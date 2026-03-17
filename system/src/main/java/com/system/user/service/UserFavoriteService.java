package com.system.user.service;

import com.system.user.entity.FavoriteView;

import java.util.List;

public interface UserFavoriteService {
    void add(Long userId, Long goodsId);

    void remove(Long userId, Long goodsId);

    boolean exists(Long userId, Long goodsId);

    List<FavoriteView> list(Long userId);

    int count(Long userId);

    int removeBatch(Long userId, List<Long> goodsIds);

    int cleanupInvalid(Long userId);
}

