package com.alexhong.petchill.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.petchill.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:26:09
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

