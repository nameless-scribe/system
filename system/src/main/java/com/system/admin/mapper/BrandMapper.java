package com.system.admin.mapper;

import com.system.admin.entity.Brand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BrandMapper {

    Brand selectById(@Param("id") Long id);

    List<Brand> selectAll();

    int insert(Brand brand);

    int update(Brand brand);

    int deleteById(@Param("id") Long id);
}

