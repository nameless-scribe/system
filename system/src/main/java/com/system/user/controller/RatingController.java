package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.Rating;
import com.system.user.entity.User;
import com.system.user.service.RatingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    private User currentUser(HttpServletRequest request) {
        // 由拦截器基于 token 设置
        return (User) request.getAttribute("currentUser");
    }

    /**
     * 对交易对象进行评价（买家评价卖家，或卖家评价买家）
     */
    @PostMapping
    public Result<Void> add(@RequestBody Rating rating, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (rating == null) {
            return Result.fail("评价内容不能为空");
        }
        if (rating.getOrderId() == null) {
            return Result.fail("订单ID不能为空");
        }
        if (rating.getToUserId() == null) {
            return Result.fail("被评价用户不能为空");
        }
        if (rating.getScore() == null || rating.getScore() < 1 || rating.getScore() > 5) {
            return Result.fail("评分必须在 1-5 之间");
        }
        if (rating.getComment() != null && rating.getComment().length() > 500) {
            return Result.fail("评价内容长度不能超过 500 个字符");
        }
        rating.setFromUserId(user.getId());

        // 防止重复评价同一订单
        Rating exists = ratingService.findByOrderAndFromUser(rating.getOrderId(), rating.getFromUserId());
        if (exists != null) {
            return Result.fail("该订单已评价");
        }

        ratingService.addRating(rating);
        return Result.success();
    }

    /**
     * 我收到的评价列表
     */
    @GetMapping("/received")
    public Result<List<Rating>> received(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        List<Rating> list = ratingService.listReceived(user.getId());
        return Result.success(list);
    }
}

