package com.alexhong.petchill.product.service;

import com.alexhong.petchill.product.entity.SpuInfoDescEntity;
import com.alexhong.petchill.product.vo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.petchill.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 21:13:39
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    PageUtils up(Long spuId);
}

