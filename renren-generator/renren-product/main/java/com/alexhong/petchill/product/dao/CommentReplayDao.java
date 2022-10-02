package com.alexhong.petchill.product.dao;

import com.alexhong.petchill.product.entity.CommentReplayEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价回复关系
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 21:13:40
 */
@Mapper
public interface CommentReplayDao extends BaseMapper<CommentReplayEntity> {
	
}
