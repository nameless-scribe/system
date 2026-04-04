package com.system.user.service;

import com.system.user.entity.Rating;
import com.system.user.entity.GoodsCommentView;

import java.util.List;

public interface RatingService {

    void addRating(Rating rating);

    List<Rating> listReceived(Long toUserId);

    Rating findByOrderAndFromUser(Long orderId, Long fromUserId);

    List<GoodsCommentView> listGoodsComments(Long goodsId, int size);

    void updateRating(Long id, Integer score, String comment);

    List<Rating> listByFromUser(Long fromUserId);

    void deleteById(Long id, Long fromUserId);
}

