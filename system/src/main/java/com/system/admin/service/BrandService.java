package com.system.admin.service;

import com.system.admin.entity.Brand;

import java.util.List;

public interface BrandService {

    Brand getById(Long id);

    List<Brand> listAll();

    void create(Brand brand);

    void update(Brand brand);

    void delete(Long id);
}

