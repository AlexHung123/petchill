package com.alexhong.petchill.member.dao;

import com.alexhong.petchill.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:28:02
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {

    /***
     * Return the default level of member
     * @return
     */
    MemberLevelEntity getDefaultLevel();
}
