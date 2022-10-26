package com.alexhong.petchill.product.feign;

import com.alexhong.common.to.SkuHasStockVo;
import com.alexhong.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("petchill-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hastock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
