package com.system.admin.service;

import com.system.admin.entity.Report;

import java.util.List;

public interface ReportService {

    void submit(Report report);

    List<Report> listAll();

    void handle(Report report);
}

