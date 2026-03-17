package com.system.admin.service.impl;

import com.system.admin.entity.Brand;
import com.system.admin.mapper.BrandMapper;
import com.system.admin.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public Brand getById(Long id) {
        return brandMapper.selectById(id);
    }

    @Override
    public List<Brand> listAll() {
        return brandMapper.selectAll();
    }

    @Override
    public void create(Brand brand) {
        validateBrand(brand);
        brandMapper.insert(brand);
    }

    @Override
    public void update(Brand brand) {
        validateBrand(brand);
        brandMapper.update(brand);
    }

    @Override
    public void delete(Long id) {
        brandMapper.deleteById(id);
    }

    /**
     * 品牌基础校验：名称必填，长度限制等
     */
    private void validateBrand(Brand brand) {
        if (brand == null) {
            throw new IllegalArgumentException("品牌信息不能为空");
        }
        String name = brand.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("品牌名称不能为空");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("品牌名称长度不能超过 50 个字符");
        }
        String desc = brand.getDescription();
        if (desc != null && desc.length() > 500) {
            throw new IllegalArgumentException("品牌描述长度不能超过 500 个字符");
        }
    }
}

