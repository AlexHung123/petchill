package com.alexhong.petchill.product.service.impl;

import com.alexhong.petchill.product.service.CategoryBrandRelationService;
import com.alexhong.petchill.product.vo.Catelog2Vo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.netty.util.internal.StringUtil;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.common.utils.Query;

import com.alexhong.petchill.product.dao.CategoryDao;
import com.alexhong.petchill.product.entity.CategoryEntity;
import com.alexhong.petchill.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

//    private Map<String, Object> cache = new HashMap<>();

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedissonClient redisson;

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
                map((menu) -> {
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

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        ArrayList<Long> paths = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categories'"),
//            @CacheEvict(value = "category", key = "'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updataCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)  //mean current method's result need to be cached
    @Override
    public List<CategoryEntity> getLevel1Categories() {
        System.out.println("store data to cache category");
        List<CategoryEntity> level1Categories = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return level1Categories;
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("---Query in the db---" + Thread.currentThread().getId());

        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
        //1. get all categories
        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);
        //2. package data
        Map<String, List<Catelog2Vo>> collect2 = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // level 2 categories for level 1
            List<CategoryEntity> level2Categories = getParent_cid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;

            if (level2Categories != null) {
                catelog2Vos = level2Categories.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //search level 3 categories for current level 2 category
                    List<CategoryEntity> level3Categories = getParent_cid(selectList, l2.getCatId());
                    if (level3Categories != null) {
                        List<Catelog2Vo.Catelog3Vo> collect1 = level3Categories.stream().map(l3 -> {

                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;

                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect1);

                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));
        return collect2;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJson2() {

        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtil.isNullOrEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisLock();
            return catalogJsonFromDb;
        }
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;
    }

    /***
     * sync data in cache with database
     * cache data consistency 
     * @return
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedissonLock() {

        RLock lock2 = redisson.getLock("catalogJson-lock");

        lock2.lock();
        System.out.println("Get Distributed Locks successfully");
        Map<String, List<Catelog2Vo>> dataFromDb = null;
        try {
            dataFromDb = getDataFromDb();
        } finally {
            lock2.unlock();
        }
        return dataFromDb;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLock() {

        String uuid = UUID.randomUUID().toString();
        //distributed lock
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("Get Distributed Locks successfully");
            Map<String, List<Catelog2Vo>> dataFromDb = null;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
                //删除锁：原子操作，查询和删除一起：LUA脚本
                Integer lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Integer>(script, Integer.class)
                        , Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            //retry when fail to get the lock
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return getCatalogJsonFromDbWithRedisLock();
        }

    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        //check the cache again when getting the key, query db if not found
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtil.isNullOrEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("---Query in the db---" + Thread.currentThread().getId());

        List<CategoryEntity> selectList = this.baseMapper.selectList(null);
        //1. get all categories
        List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);
        //2. package data
        Map<String, List<Catelog2Vo>> collect2 = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // level 2 categories for level 1
            List<CategoryEntity> level2Categories = getParent_cid(selectList, v.getCatId());
            List<Catelog2Vo> catelog2Vos = null;

            if (level2Categories != null) {
                catelog2Vos = level2Categories.stream().map(l2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                    //search level 3 categories for current level 2 category
                    List<CategoryEntity> level3Categories = getParent_cid(selectList, l2.getCatId());
                    if (level3Categories != null) {
                        List<Catelog2Vo.Catelog3Vo> collect1 = level3Categories.stream().map(l3 -> {

                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                            return catelog3Vo;

                        }).collect(Collectors.toList());
                        catelog2Vo.setCatalog3List(collect1);

                    }
                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));
        //update data from db to redis cache, change object to json format
        String s = JSON.toJSONString(collect2);
        stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
        return collect2;
    }


    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {

        //if exists in cache, then get data from cache
//        Map<String, List<Catelog2Vo>> catalogJson = (Map<String, List<Catelog2Vo>>) cache.get("catalogJson");
//        if(catalogJson == null){
//
//        }
//        return catalogJson;

        synchronized (this) {
            //check the cache again when getting the key, query db if not found
            String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
            if (!StringUtil.isNullOrEmpty(catalogJSON)) {
                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
                return result;
            }
            System.out.println("---Query in the db---" + Thread.currentThread().getId());

            /***
             * change multiple query from db to once
             */
            List<CategoryEntity> selectList = this.baseMapper.selectList(null);
            //1. get all categories
            List<CategoryEntity> level1Categories = getParent_cid(selectList, 0L);
            //2. package data
            Map<String, List<Catelog2Vo>> collect2 = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                // level 2 categories for level 1
                List<CategoryEntity> level2Categories = getParent_cid(selectList, v.getCatId());
                List<Catelog2Vo> catelog2Vos = null;

                if (level2Categories != null) {
                    catelog2Vos = level2Categories.stream().map(l2 -> {
                        Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, l2.getCatId().toString(), l2.getName());
                        //search level 3 categories for current level 2 category
                        List<CategoryEntity> level3Categories = getParent_cid(selectList, l2.getCatId());
                        if (level3Categories != null) {
                            List<Catelog2Vo.Catelog3Vo> collect1 = level3Categories.stream().map(l3 -> {

                                Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(l2.getCatId().toString(), l3.getCatId().toString(), l3.getName());
                                return catelog3Vo;

                            }).collect(Collectors.toList());
                            catelog2Vo.setCatalog3List(collect1);

                        }
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                }

                return catelog2Vos;
            }));
            //update data from db to redis cache, change object to json format
            String s = JSON.toJSONString(collect2);
            stringRedisTemplate.opsForValue().set("catalogJSON", s, 1, TimeUnit.DAYS);
            return collect2;
        }
    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return collect;
    }

    //225, 25, 2
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(categoryEntity -> categoryEntity.getParentCid() == root.getCatId()).map(categoryEntity -> {
            //1. find the sub-categories
            categoryEntity.setChildren(getChildren(categoryEntity, all));
            return categoryEntity;
        }).sorted(Comparator.comparingInt(menu -> (menu.getSort() == null ? 0 : menu.getSort()))).collect(Collectors.toList());

        return children;
    }

}