package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.FavoriteView;
import com.system.user.entity.User;
import com.system.user.service.UserFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户端：收藏
 */
@RestController
@RequestMapping("/api/user/favorites")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService favoriteService;

    private User currentUser(HttpServletRequest request) {
        return (User) request.getAttribute("currentUser");
    }

    @GetMapping
    public Result<List<FavoriteView>> list(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        return Result.success(favoriteService.list(user.getId()));
    }

    @GetMapping("/count")
    public Result<Integer> count(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        return Result.success(favoriteService.count(user.getId()));
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        Long goodsId = body == null ? null : toLong(body.get("goodsId"));
        if (goodsId == null) {
            return Result.fail("goodsId 不能为空");
        }
        favoriteService.add(user.getId(), goodsId);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> remove(@RequestParam Long goodsId, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        favoriteService.remove(user.getId(), goodsId);
        return Result.success();
    }

    @DeleteMapping("/batch")
    public Result<Integer> removeBatch(@RequestBody List<Long> goodsIds, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        return Result.success(favoriteService.removeBatch(user.getId(), goodsIds));
    }

    @PostMapping("/cleanupInvalid")
    public Result<Integer> cleanupInvalid(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        return Result.success(favoriteService.cleanupInvalid(user.getId()));
    }

    @GetMapping("/exists")
    public Result<Boolean> exists(@RequestParam Long goodsId, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.success(false);
        }
        return Result.success(favoriteService.exists(user.getId(), goodsId));
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(String.valueOf(val));
        } catch (Exception e) {
            return null;
        }
    }
}

