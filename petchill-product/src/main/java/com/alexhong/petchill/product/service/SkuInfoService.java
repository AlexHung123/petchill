package com.alexhong.petchill.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.petchill.product.entity.SkuInfoEntity;

import java.util.Map;

/**
 * sku信息
 *
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 21:13:39
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);
}

