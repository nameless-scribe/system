package com.system.user.controller;

import com.system.admin.entity.Goods;
import com.system.admin.service.GoodsService;
import com.system.common.Result;
import com.system.user.entity.User;
import com.system.user.entity.UserProfile;
import com.system.user.entity.UserReputation;
import com.system.user.service.UserProfileService;
import com.system.user.service.UserReputationService;
import com.system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 前台：卖家店铺信息
 */
@RestController
@RequestMapping("/api/shop/sellers")
public class ShopSellerController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserReputationService userReputationService;

    @Autowired
    private GoodsService goodsService;

    /**
     * 卖家店铺信息：基础信息 + 信誉 + 在售商品
     */
    @GetMapping("/{sellerId}/shopInfo")
    public Result<Map<String, Object>> shopInfo(@PathVariable Long sellerId,
                                                @RequestParam(defaultValue = "6") int goodsSize) {
        if (sellerId == null) {
            return Result.fail("卖家ID不能为空");
        }

        User seller = userService.getById(sellerId);
        if (seller == null) {
            return Result.fail("卖家不存在");
        }

        UserProfile profile = userProfileService.getByUserId(sellerId);
        UserReputation rep = userReputationService.getByUserId(sellerId);

        int size = goodsSize <= 0 ? 6 : Math.min(goodsSize, 20);
        List<Goods> onSale = goodsService.listOnSaleByOwnerId(sellerId, size);
        int onSaleCount = goodsService.countOnSaleByOwnerId(sellerId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("sellerId", seller.getId());
        resp.put("username", seller.getUsername());
        resp.put("createTime", seller.getCreateTime());
        resp.put("joinedDays", joinedDays(seller.getCreateTime()));

        // profile：前台不返回 phone/email 这类敏感字段
        Map<String, Object> safeProfile = new HashMap<>();
        if (profile != null) {
            safeProfile.put("avatar", profile.getAvatar());
            safeProfile.put("intro", profile.getIntro());
            safeProfile.put("gender", profile.getGender());
        }
        resp.put("profile", safeProfile);

        // reputation
        resp.put("reputation", rep);

        // goods
        resp.put("onSaleGoods", onSale);
        resp.put("onSaleCount", onSaleCount);
        return Result.success(resp);
    }

    private long joinedDays(java.util.Date createTime) {
        if (createTime == null) {
            return 0;
        }
        Instant start = createTime.toInstant();
        Instant now = Instant.now();
        long days = Duration.between(start, now).toDays();
        return Math.max(days, 0);
    }
}

