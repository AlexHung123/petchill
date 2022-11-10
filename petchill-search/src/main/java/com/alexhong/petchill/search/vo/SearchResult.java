package com.alexhong.petchill.search.vo;

import com.alexhong.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    //all product info
    private List<SkuEsModel> product;

    /***
     * page info
     */
    private Integer pageNum;
    //records
    private Long total;
    private Integer totalPages;
    private List<BrandVo> brands;//all brands related to current search result
    private List<CatalogVo> catalogs;
    private List<AttrVo> attrs;


    @Data
    public static class BrandVo{
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo{
        private Long catalogId;
        private String catalogName;
    }

}
