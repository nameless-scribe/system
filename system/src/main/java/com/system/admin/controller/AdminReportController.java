package com.system.admin.controller;

import com.system.admin.entity.Report;
import com.system.admin.service.ReportService;
import com.system.common.Result;
import com.system.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端举报处理
 */
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping
    public Result<List<Report>> list() {
        return Result.success(reportService.listAll());
    }

    @PutMapping("/{id}")
    public Result<Void> handle(@PathVariable Long id, @RequestBody Report req, HttpServletRequest request) {
        User admin = (User) request.getAttribute("currentUser");
        if (admin == null) {
            return Result.fail("未登录");
        }
        Report r = new Report();
        r.setId(id);
        r.setStatus(req.getStatus());
        r.setResult(req.getResult());
        r.setHandlerId(admin.getId());
        reportService.handle(r);
        return Result.success();
    }
}

