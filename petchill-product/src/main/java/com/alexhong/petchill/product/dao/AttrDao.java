package com.alexhong.petchill.product.dao;

import com.alexhong.petchill.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 21:13:39
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}
