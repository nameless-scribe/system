package com.system.admin.entity;

import lombok.Data;

import java.util.Date;

/**
 * 平台公告
 */
@Data
public class Announcement {

    private Long id;
    private String title;
    private String content;
    private Date createTime;
    private Integer status; // 1-发布 0-草稿/下架
}

