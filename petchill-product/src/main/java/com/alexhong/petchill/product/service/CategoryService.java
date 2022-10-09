package com.alexhong.petchill.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.petchill.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 21:13:39
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);
}

