package com.alexhong.petchill.auth.controller;

import com.alexhong.common.constant.AuthServerConstant;
import com.alexhong.common.exception.BizCodeEnume;
import com.alexhong.common.utils.R;
import com.alexhong.petchill.auth.feign.MemberFeignService;
import com.alexhong.petchill.auth.feign.ThirdPartyFeignService;
import com.alexhong.petchill.auth.vo.UserLoginVo;
import com.alexhong.petchill.auth.vo.UserRegistVo;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone){

        //1. not allow to send twice within 60s
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMA_CODE_CACHE_PREFIX + phone);
       if(StringUtils.isEmpty(redisCode)){
           long l = Long.parseLong(redisCode.split("_")[1]);
           if(System.currentTimeMillis() - l < 60000){
               return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
           }
       }
        //2. verify code redis
        String code = UUID.randomUUID().toString().replace("-", "").substring(0, 5);
        String subString = code + System.currentTimeMillis();
        //cache the verification code, prevent the same phone no. to send the code again in 60s
        redisTemplate.opsForValue().set(AuthServerConstant.SMA_CODE_CACHE_PREFIX + phone, subString, 10, TimeUnit.MINUTES);

        thirdPartyFeignService.sendCode(phone, code);
        return R.ok();
    }

    //TODO redirect data, put data to session and delete the data when redict page get the data from the session
    //TODO distributed system for session issue
    @PostMapping("/register")
    public String register(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes){

        if(result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.petchill.com/reg.html";
        }
        //check code
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMA_CODE_CACHE_PREFIX + vo.getPhone());
        if(!StringUtils.isEmpty(s)){
            if(code.equals(s.split("_")[0])){
                redisTemplate.delete(AuthServerConstant.SMA_CODE_CACHE_PREFIX + vo.getPhone());

                //check code successfully
                R r = memberFeignService.register(vo);
                if(r.getCode() == 0){
                    //success
                    return "redirect:http://auth.petchill.com//login.html";
                }else {
                    //fail
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg",new TypeReference<String>(){}));
                    return "redirect:http://auth.petchill.com/reg.html";
                }

            }else {
                Map<String, String> errors = new HashMap<>();
                errors.put("errors", "Verification code error");
                return "redirect:http://auth.petchill.com/reg.html";
            }
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("errors", "Verification code error");
            return "redirect:http://auth.petchill.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes){

        R login = memberFeignService.login(vo);
        if(login.getCode() == 0){
            return "redirect:http://petchill.com";
        }else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.petchill.com/login.html";
        }

    }


}
