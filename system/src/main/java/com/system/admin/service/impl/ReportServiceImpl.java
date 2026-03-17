package com.system.admin.service.impl;

import com.system.admin.entity.Report;
import com.system.admin.mapper.ReportMapper;
import com.system.admin.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public void submit(Report report) {
        if (report.getStatus() == null) {
            report.setStatus("PENDING");
        }
        reportMapper.insert(report);
    }

    @Override
    public List<Report> listAll() {
        return reportMapper.selectAll();
    }

    @Override
    public void handle(Report report) {
        report.setHandleTime(new Date());
        reportMapper.update(report);
    }
}

