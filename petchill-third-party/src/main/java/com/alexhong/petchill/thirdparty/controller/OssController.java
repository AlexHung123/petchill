package com.alexhong.petchill.thirdparty.controller;

import com.alexhong.common.utils.R;
import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OssController {

    @Value("${qiniu.AccessKey}")
    private String QINIU_AK;

    @Value("${qiniu.SecretKey}")
    private String QINIU_SK;

    @Value("${qiniu.Bucket}")
    private String QINIU_BUCKET;

    @RequestMapping("/oss/policy")
    public R policy(){

        Map<String, String> respMap = new HashMap<>();
        Auth auth = Auth.create(QINIU_AK, QINIU_SK);
        String upToken = auth.uploadToken(QINIU_BUCKET);
        System.out.println(QINIU_AK);
        System.out.println(QINIU_SK);
        System.out.println(QINIU_BUCKET);



        respMap.put("upToken", upToken);
        return R.ok().put("data", respMap);

    }
}
