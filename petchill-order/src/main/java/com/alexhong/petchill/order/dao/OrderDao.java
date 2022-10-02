package com.alexhong.petchill.order.dao;

import com.alexhong.petchill.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:26:09
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
