package com.system.admin.controller;

import com.system.admin.entity.Brand;
import com.system.admin.service.BrandService;
import com.system.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public Result<List<Brand>> list() {
        return Result.success(brandService.listAll());
    }

    @GetMapping("/{id}")
    public Result<Brand> detail(@PathVariable Long id) {
        return Result.success(brandService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Brand brand) {
        brandService.create(brand);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Brand brand) {
        brand.setId(id);
        brandService.update(brand);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        brandService.delete(id);
        return Result.success();
    }
}

