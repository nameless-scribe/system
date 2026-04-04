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

    /**
     * 查询“我对某订单”的评价（用于前端判断是否已评价/编辑）
     */
    @GetMapping("/my")
    public Result<Rating> my(@RequestParam("orderId") Long orderId, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (orderId == null) {
            return Result.fail("订单ID不能为空");
        }
        Rating r = ratingService.findByOrderAndFromUser(orderId, user.getId());
        return Result.success(r);
    }

    /**
     * 编辑我对该订单已提交的评价（仅允许修改分数与评语）
     */
    @PutMapping
    public Result<Void> update(@RequestBody Rating rating, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        if (rating == null || rating.getOrderId() == null) {
            return Result.fail("订单ID不能为空");
        }
        if (rating.getScore() == null || rating.getScore() < 1 || rating.getScore() > 5) {
            return Result.fail("评分必须在 1-5 之间");
        }
        if (rating.getComment() != null && rating.getComment().length() > 500) {
            return Result.fail("评价内容长度不能超过 500 个字符");
        }
        Rating exists = ratingService.findByOrderAndFromUser(rating.getOrderId(), user.getId());
        if (exists == null) {
            return Result.fail("尚未评价，无法编辑");
        }
        ratingService.updateRating(exists.getId(), rating.getScore(), rating.getComment());
        return Result.success();
    }

    /**
     * 我发表过的评价列表
     */
    @GetMapping("/mine")
    public Result<List<Rating>> mine(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        return Result.success(ratingService.listByFromUser(user.getId()));
    }

    /**
     * 删除我发表的某条评价
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        ratingService.deleteById(id, user.getId());
        return Result.success();
    }
}

