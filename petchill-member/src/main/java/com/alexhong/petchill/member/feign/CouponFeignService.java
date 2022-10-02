package com.alexhong.petchill.member.feign;


import com.alexhong.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("petchill-coupon")
public interface CouponFeignService
{
    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
