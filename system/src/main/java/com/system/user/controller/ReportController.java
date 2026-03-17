package com.system.user.controller;

import com.system.admin.entity.Report;
import com.system.admin.service.ReportService;
import com.system.common.Result;
import com.system.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端提交举报
 */
@RestController
@RequestMapping("/api/user/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public Result<Void> submit(@RequestBody Report report, HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        if (user == null) {
            return Result.fail("未登录");
        }
        if (report == null) {
            return Result.fail("举报内容不能为空");
        }
        if (report.getTargetType() == null || report.getTargetType().trim().isEmpty()) {
            return Result.fail("举报类型不能为空");
        }
        if (report.getTargetId() == null) {
            return Result.fail("举报目标不能为空");
        }
        if (report.getReason() == null || report.getReason().trim().isEmpty()) {
            return Result.fail("举报原因不能为空");
        }
        if (report.getReason().length() > 500) {
            return Result.fail("举报原因长度不能超过 500 个字符");
        }
        report.setReporterId(user.getId());
        reportService.submit(report);
        return Result.success();
    }
}

