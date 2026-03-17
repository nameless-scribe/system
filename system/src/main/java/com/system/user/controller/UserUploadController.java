package com.system.user.controller;

import com.system.common.Result;
import com.system.util.UploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用户端图片上传接口（登录用户即可使用）
 */
@RestController
@RequestMapping("/api/user/upload")
public class UserUploadController {

    @Autowired
    private UploadUtil uploadUtil;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String url = uploadUtil.uploadGoodsImg(file);
        return Result.success(url);
    }
}

