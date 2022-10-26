package com.alexhong.petchill.product.feign;

import com.alexhong.common.to.es.SkuEsModel;
import com.alexhong.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("petchill-search")
public interface SearchFeighService {

    @PostMapping("/search/save/product")
    R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
