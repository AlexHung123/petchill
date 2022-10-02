package com.alexhong.petchill.member.dao;

import com.alexhong.petchill.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:28:03
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
