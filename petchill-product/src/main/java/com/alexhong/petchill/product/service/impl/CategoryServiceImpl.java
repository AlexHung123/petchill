package com.alexhong.petchill.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.common.utils.Query;

import com.alexhong.petchill.product.dao.CategoryDao;
import com.alexhong.petchill.product.entity.CategoryEntity;
import com.alexhong.petchill.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1. get all categories
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        //2. tree relation
        //2.1 find all level 1 category
        List<CategoryEntity> level1Menu = categoryEntities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0).
                map((menu)->{
                    menu.setChildren(getChildren(menu, categoryEntities));
                    return menu;
                }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());
        return level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO: check current deleted menus which having any other references or not
        baseMapper.deleteBatchIds(asList);
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all){
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> categoryEntity.getParentCid() == root.getCatId()).map(categoryEntity -> {
            //1. find the sub-categories
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());

        return children;
    }

}