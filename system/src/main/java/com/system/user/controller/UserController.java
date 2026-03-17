package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.User;
import com.system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        try {
            userService.register(user);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
}

