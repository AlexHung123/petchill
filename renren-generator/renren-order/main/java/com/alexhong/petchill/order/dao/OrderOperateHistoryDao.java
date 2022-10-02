package com.alexhong.petchill.order.dao;

import com.alexhong.petchill.order.entity.OrderOperateHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单操作历史记录
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:26:08
 */
@Mapper
public interface OrderOperateHistoryDao extends BaseMapper<OrderOperateHistoryEntity> {
	
}
