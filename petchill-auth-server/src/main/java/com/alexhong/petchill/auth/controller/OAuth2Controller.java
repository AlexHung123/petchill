package com.alexhong.petchill.auth.controller;

import com.alexhong.common.utils.HttpUtils;
import com.alexhong.common.utils.R;
import com.alexhong.petchill.auth.feign.MemberFeignService;
import com.alexhong.common.vo.MemberRespVo;
import com.alexhong.petchill.auth.vo.SocialUser;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String, String> map = new HashMap<>();
        map.put("client_id", "");
        map.put("client_secret", "");
        map.put("grant_type", "");
        map.put("redirect_uri", "");
        map.put("code", code);

        //1. using authCode to get accessToken
        HttpResponse httpResponse = HttpUtils.doPost("api.weibo.com", "/auth2/access_token", "post", null, null, map);

        //2. handle
        if(httpResponse.getStatusLine().getStatusCode() == 200){
            String json = EntityUtils.toString(httpResponse.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);

            //1) if current user first login, auto register
            //login or register this social account
            R oauthlogin = memberFeignService.oauthlogin(socialUser);
            if(oauthlogin.getCode() ==0){
                MemberRespVo data = oauthlogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("login successfully {}", data);
                //TODO 1. session=esfsdkddd Current scope not enough, need to handle the sharing session for sub domain
                //TODO 2. using json Serializable
                session.setAttribute("loginUser", data);
                return "redirect:http://petchill.com";
            }else {
                return "redirect:http://auth.petchill.com/login.html";
            }

        }else {
            return "redirect:http://auth.petchill.com/login.html";
        }


    }
}
