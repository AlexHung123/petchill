package com.alexhong.petchill.product.service;

import com.alexhong.petchill.product.entity.BrandEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.petchill.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 21:13:39
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updataCategory(Long catId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

