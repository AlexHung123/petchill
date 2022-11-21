package com.alexhong.petchill.search.vo;

import com.alexhong.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
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
    private List<Integer> pageNavs;
    private List<BrandVo> brands;//all brands related to current search result
    private List<CatalogVo> catalogs;
    private List<AttrVo> attrs;

    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }


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
