package com.system.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件上传工具（从 SSM 项目迁移）
 */
@Service
public class UploadUtil {

    @Value("${upload.temp.path}")
    private String tempPath;

    @Value("${upload.img.path}")
    private String imgPath;

    @Value("${upload.img.accessPath}")
    private String imgAccessPath;

    /**
     * 处理商品图片上传，返回数据库存储的路径（如：/img/20240520/xxx.jpg）
     */
    public String uploadGoodsImg(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        String dateDir = new SimpleDateFormat("yyyyMMdd").format(new Date());
        File saveDir = new File(imgPath + dateDir);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String fileName = UUID.randomUUID().toString() + suffix;

        File destFile = new File(saveDir, fileName);
        file.transferTo(destFile);

        return imgAccessPath + dateDir + "/" + fileName;
    }
}

