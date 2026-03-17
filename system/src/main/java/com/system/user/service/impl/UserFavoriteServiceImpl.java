package com.system.user.service.impl;

import com.system.user.entity.FavoriteView;
import com.system.user.entity.UserFavorite;
import com.system.user.mapper.UserFavoriteMapper;
import com.system.user.service.UserFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFavoriteServiceImpl implements UserFavoriteService {

    @Autowired
    private UserFavoriteMapper favoriteMapper;

    /**
     * 收藏上限（默认 200）：可通过 application.yml 配置或环境变量等覆盖。
     */
    @Value("${app.favorite.max:200}")
    private int maxFavorite;

    @Override
    public void add(Long userId, Long goodsId) {
        if (userId == null || goodsId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        int current = favoriteMapper.countByUserId(userId);
        if (maxFavorite > 0 && current >= maxFavorite) {
            throw new IllegalArgumentException("收藏数量已达上限（" + maxFavorite + "）");
        }
        UserFavorite f = new UserFavorite();
        f.setUserId(userId);
        f.setGoodsId(goodsId);
        try {
            favoriteMapper.insert(f);
        } catch (DuplicateKeyException ignore) {
            // 已收藏：幂等
        }
    }

    @Override
    public void remove(Long userId, Long goodsId) {
        if (userId == null || goodsId == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        favoriteMapper.delete(userId, goodsId);
    }

    @Override
    public boolean exists(Long userId, Long goodsId) {
        if (userId == null || goodsId == null) {
            return false;
        }
        return favoriteMapper.countByUserAndGoods(userId, goodsId) > 0;
    }

    @Override
    public List<FavoriteView> list(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return favoriteMapper.selectByUserId(userId);
    }

    @Override
    public int count(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return favoriteMapper.countByUserId(userId);
    }

    @Override
    public int removeBatch(Long userId, List<Long> goodsIds) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        if (goodsIds == null || goodsIds.isEmpty()) {
            return 0;
        }
        return favoriteMapper.deleteBatch(userId, goodsIds);
    }

    @Override
    public int cleanupInvalid(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("未登录");
        }
        return favoriteMapper.deleteInvalidByUserId(userId);
    }
}

