package com.alexhong.petchill.ssoclient.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: HelloController
 * Package: com.alexhong.petchill.ssoclient.controller
 * Description:
 *
 * @Author: Alex Hong
 * @Creatr: 25/11/2022 - 12:23 am
 */
@Controller
public class HelloController {

    @Value("${sso.server.url}")
    String ssoServerUrl;

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    /***
     *
     * @param model
     * @param session
     * @param token if login ssoserver successfully, then get token value
     * @return
     */
    @GetMapping("/boss")
    public String employees(Model model, HttpSession session, @RequestParam(value="token", required = false) String token){

        if(!StringUtils.isEmpty(token)){
            //TODO get user info in ssoserver
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://ssoserver.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            session.setAttribute("loginUser",body);
        }

        Object loginUser = session.getAttribute("loginUser");
        if(loginUser==null){
            return "redirect:" + ssoServerUrl + "?redirect_url=http://client2.com:8082/boss";
        }else {
            List<String> emps = new ArrayList<>();
            emps.add("alex");
            emps.add("bob");
            model.addAttribute("emps", emps);
            return "list";
        }
    }
}
