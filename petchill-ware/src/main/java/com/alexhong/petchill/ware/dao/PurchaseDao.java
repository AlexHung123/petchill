package com.alexhong.petchill.ware.dao;

import com.alexhong.petchill.ware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:29:54
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
