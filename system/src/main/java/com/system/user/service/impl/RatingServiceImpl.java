package com.system.user.service.impl;

import com.system.user.entity.GoodsCommentView;
import com.system.user.entity.Rating;
import com.system.user.mapper.RatingMapper;
import com.system.user.service.RatingService;
import com.system.user.service.UserReputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingMapper ratingMapper;

    @Autowired
    private UserReputationService userReputationService;

    @Override
    public void addRating(Rating rating) {
        rating.setCreatedTime(new Date());
        ratingMapper.insert(rating);
        // 新增评价后，更新被评价用户的信誉信息
        userReputationService.updateOnNewRating(rating);
    }

    @Override
    public List<Rating> listReceived(Long toUserId) {
        return ratingMapper.selectByToUserId(toUserId);
    }

    @Override
    public Rating findByOrderAndFromUser(Long orderId, Long fromUserId) {
        return ratingMapper.selectByOrderAndFromUser(orderId, fromUserId);
    }

    @Override
    public List<GoodsCommentView> listGoodsComments(Long goodsId, int size) {
        if (goodsId == null) {
            return List.of();
        }
        int s = size <= 0 ? 10 : Math.min(size, 50);
        return ratingMapper.selectCommentsByGoodsId(goodsId, s);
    }

    @Override
    public void updateRating(Long id, Integer score, String comment) {
        ratingMapper.updateById(id, score, comment);
    }

    @Override
    public List<Rating> listByFromUser(Long fromUserId) {
        return ratingMapper.selectByFromUserId(fromUserId);
    }

    @Override
    public void deleteById(Long id, Long fromUserId) {
        ratingMapper.deleteByIdAndFromUser(id, fromUserId);
    }
}

