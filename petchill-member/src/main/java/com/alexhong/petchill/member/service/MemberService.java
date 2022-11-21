package com.alexhong.petchill.member.service;

import com.alexhong.petchill.member.exception.PhoneExistException;
import com.alexhong.petchill.member.exception.UsernameExistException;
import com.alexhong.petchill.member.vo.MemberLoginVo;
import com.alexhong.petchill.member.vo.MemberRegisterVo;
import com.alexhong.petchill.member.vo.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.petchill.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author alexhong
 * @email yifenghung123@gmail.com
 * @date 2022-10-01 22:28:03
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);

    void checkPhoneUnique(String email) throws PhoneExistException;

    void checkUsernameUnique(String username) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser) throws Exception;
}

