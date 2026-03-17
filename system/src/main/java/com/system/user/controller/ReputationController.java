package com.system.user.controller;

import com.system.common.Result;
import com.system.user.entity.UserReputation;
import com.system.user.service.UserReputationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/reputation")
public class ReputationController {

    @Autowired
    private UserReputationService userReputationService;

    @GetMapping("/{userId}")
    public Result<UserReputation> get(@PathVariable Long userId) {
        UserReputation rep = userReputationService.getByUserId(userId);
        if (rep == null) {
            return Result.fail("暂无信誉数据");
        }
        return Result.success(rep);
    }
}

