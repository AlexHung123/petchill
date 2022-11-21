package com.alexhong.petchill.thirdparty.controller;


import com.alexhong.common.utils.R;
import com.alexhong.petchill.thirdparty.component.SmsComponent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsSendController {


    private SmsComponent smsComponent;
    /***
     *
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("sendcode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code){
        smsComponent.sendCode(phone, code);
        return R.ok();
    }
}
