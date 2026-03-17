package com.system.user.entity;

import lombok.Data;

import java.util.Date;

/**
 * 用户详细资料表：与 user 表一对一，通过 user_id 关联
 */
@Data
public class UserProfile {

    private Long id;
    private Long userId;
    private String realName;
    private String phone;
    private String email;
    private Date birthday;
    private Integer age;
    private String gender;   // M/F/OTHER 或者 男/女
    private String avatar;   // 头像地址（可选）
    private String intro;    // 个性签名/简介（可选）
}

