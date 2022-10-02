package com.alexhong.petchill.coupon.dao;

import com.alexhong.petchill.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:23:08
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
