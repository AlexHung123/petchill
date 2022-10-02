package com.alexhong.petchill.coupon.dao;

import com.alexhong.petchill.coupon.entity.CouponSpuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券与产品关联
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:23:08
 */
@Mapper
public interface CouponSpuRelationDao extends BaseMapper<CouponSpuRelationEntity> {
	
}
