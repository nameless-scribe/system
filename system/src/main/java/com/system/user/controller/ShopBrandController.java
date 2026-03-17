package com.system.user.controller;

import com.system.admin.entity.Brand;
import com.system.admin.service.BrandService;
import com.system.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台分类（品牌）浏览接口：所有用户可访问
 */
@RestController
@RequestMapping("/api/shop/brands")
public class ShopBrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public Result<List<Brand>> list() {
        return Result.success(brandService.listAll());
    }
}

