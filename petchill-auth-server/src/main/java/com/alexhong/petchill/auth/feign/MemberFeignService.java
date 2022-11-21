package com.alexhong.petchill.auth.feign;

import com.alexhong.common.utils.R;
import com.alexhong.petchill.auth.vo.SocialUser;
import com.alexhong.petchill.auth.vo.UserLoginVo;
import com.alexhong.petchill.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("petchill-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegistVo vo);


    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthlogin(@RequestBody SocialUser socialUser) throws Exception;
}
