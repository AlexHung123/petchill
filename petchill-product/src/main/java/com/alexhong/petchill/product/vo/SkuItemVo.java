package com.alexhong.petchill.product.vo;

import com.alexhong.petchill.product.entity.SkuImagesEntity;
import com.alexhong.petchill.product.entity.SkuInfoEntity;
import com.alexhong.petchill.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@ToString
@Data
public class SkuItemVo {

    //1. sku basa information   pms_sku_info
    SkuInfoEntity info;

    boolean hasStock = true;
    //2. sku image information  pms_sku_images
    List<SkuImagesEntity> images;
    //3. spu salary attributes
    List<SkuItemSaleAttrVo> saleAttr;
    //4. spu introduction
    SpuInfoDescEntity desc;
    //5. spu parameters information
    List<SpuItemAttrGroupVo> groupAttrs;

//    @ToString
//    @Data
//    public static class SkuItemSaleAttrVo{
//        private Long attrId;
//        private String attrName;
//        private List<String> attrValues;
//    }

//    @ToString
//    @Data
//    public static class SpuItemAttrGroupVo{
//        private String groupName;
//        private List<SpuBaseAttrVo> attrValues;
//
//    }
//
//    @ToString
//    @Data
//    public static class SpuBaseAttrVo{
//        private String attrName;
//        private String attrValue;
//    }

}
