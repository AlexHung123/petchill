package com.alexhong.petchill.coupon.service;

import com.alexhong.common.to.SkuReductionTo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.petchill.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:23:08
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo reductionTo);
}

