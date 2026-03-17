package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.User;
import com.system.user.entity.UserProfile;
import com.system.user.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    private User currentUser(HttpServletRequest request) {
        // 由拦截器基于 token 设置
        return (User) request.getAttribute("currentUser");
    }

    /**
     * 获取当前登录用户的个人资料
     */
    @GetMapping
    public Result<UserProfile> getProfile(HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        UserProfile profile = userProfileService.getByUserId(user.getId());
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(user.getId());
        }
        return Result.success(profile);
    }

    /**
     * 更新当前登录用户的个人资料
     */
    @PostMapping
    public Result<Void> saveProfile(@RequestBody UserProfile profile, HttpServletRequest request) {
        User user = currentUser(request);
        if (user == null) {
            return Result.fail("未登录");
        }
        // 基础校验：长度与格式
        if (profile.getRealName() != null && profile.getRealName().length() > 50) {
            return Result.fail("真实姓名长度不能超过 50 个字符");
        }
        if (profile.getPhone() != null && !profile.getPhone().isEmpty()) {
            if (!profile.getPhone().matches("^1[3-9]\\d{9}$")) {
                return Result.fail("手机号格式不正确");
            }
        }
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) {
            if (!profile.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return Result.fail("邮箱格式不正确");
            }
        }
        if (profile.getAge() != null && (profile.getAge() < 0 || profile.getAge() > 150)) {
            return Result.fail("年龄范围不正确");
        }
        if (profile.getIntro() != null && profile.getIntro().length() > 500) {
            return Result.fail("个人简介长度不能超过 500 个字符");
        }
        profile.setUserId(user.getId());
        userProfileService.saveOrUpdate(profile);
        return Result.success();
    }
}

