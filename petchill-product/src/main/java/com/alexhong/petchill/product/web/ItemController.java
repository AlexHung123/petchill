package com.alexhong.petchill.product.web;


import com.alexhong.petchill.product.service.SkuInfoService;
import com.alexhong.petchill.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.ExecutionException;

@Controller
public class ItemController {

    @Autowired
    SkuInfoService skuInfoService;

    //show detailed information for sku
    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable("skuId") Long skuId, Model model) throws ExecutionException, InterruptedException {
        System.out.println("query info" + skuId);
        SkuItemVo item = skuInfoService.item(skuId);
        model.addAttribute("item", item);
        return "item";
    }
}
