package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.User;
import com.system.user.service.BuyRequestService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 用户端：发布/评论/点赞求购
 */
@RestController
@RequestMapping("/api/user/buyRequests")
public class UserBuyRequestController {

    @Autowired
    private BuyRequestService buyRequestService;

    private User currentUser(HttpServletRequest request) {
        return (User) request.getAttribute("currentUser");
    }

    @PostMapping
    public Result<Void> publish(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) return Result.fail("未登录");
        String title = body == null ? null : asString(body.get("title"));
        String content = body == null ? null : asString(body.get("content"));
        BigDecimal priceMin = asBigDecimal(body == null ? null : body.get("priceMin"));
        BigDecimal priceMax = asBigDecimal(body == null ? null : body.get("priceMax"));
        String contact = body == null ? null : asString(body.get("contact"));
        buyRequestService.publish(user.getId(), title, content, priceMin, priceMax, contact);
        return Result.success();
    }

    @PostMapping("/{id}/comments")
    public Result<Void> addComment(@PathVariable Long id,
                                   @RequestBody Map<String, Object> body,
                                   HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) return Result.fail("未登录");
        String content = body == null ? null : asString(body.get("content"));
        buyRequestService.addComment(user.getId(), id, content);
        return Result.success();
    }

    @PostMapping("/{id}/like")
    public Result<Boolean> like(@PathVariable Long id, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) return Result.fail("未登录");
        return Result.success(buyRequestService.like(user.getId(), id));
    }

    @PostMapping("/{id}/unlike")
    public Result<Boolean> unlike(@PathVariable Long id, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) return Result.fail("未登录");
        return Result.success(buyRequestService.unlike(user.getId(), id));
    }

    @GetMapping("/{id}/liked")
    public Result<Boolean> liked(@PathVariable Long id, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) return Result.success(false);
        return Result.success(buyRequestService.liked(user.getId(), id));
    }

    private String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private BigDecimal asBigDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}

