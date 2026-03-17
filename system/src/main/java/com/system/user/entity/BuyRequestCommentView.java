package com.system.user.entity;

import lombok.Data;

import java.util.Date;

/**
 * 求购评论视图（带评论人信息）
 */
@Data
public class BuyRequestCommentView {
    private Long id;
    private Long requestId;
    private Long userId;
    private String content;
    private Date createTime;

    private String username;
    private String avatar;
}

