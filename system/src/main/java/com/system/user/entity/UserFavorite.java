package com.system.user.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户收藏
 */
@Data
public class UserFavorite {
    private Long id;
    private Long userId;
    private Long goodsId;
    private Date createTime;
}

