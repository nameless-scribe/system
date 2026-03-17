package com.system.admin.mapper;

import com.system.admin.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportMapper {

    int insert(Report report);

    List<Report> selectAll();

    Report selectById(@Param("id") Long id);

    int update(Report report);
}

