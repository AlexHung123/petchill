package com.alexhong.petchill.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.json.JsonData;
import com.alexhong.common.to.es.SkuEsModel;
import com.alexhong.common.utils.R;
import com.alexhong.petchill.search.constant.EsConstant;
import com.alexhong.petchill.search.feign.ProductFeignService;
import com.alexhong.petchill.search.service.MallSearchService;
import com.alexhong.petchill.search.vo.AttrResponseVo;
import com.alexhong.petchill.search.vo.BrandVo;
import com.alexhong.petchill.search.vo.SearchParam;
import com.alexhong.petchill.search.vo.SearchResult;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    ElasticsearchClient elasticsearchClient;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {

        //1. build the dsl for es query
        SearchResult result = null;
        
        //1. prepare es search request
        SearchRequest request = buildSearchRequest(param);

        //2. execute search request
        try {
            SearchResponse<Object> response = elasticsearchClient.search(request, Object.class);
            //2. convert response data format to what we need
            result = buildSearchResult(response, param);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private SearchResult buildSearchResult(SearchResponse<Object> response, SearchParam param) {

        SearchResult result = new SearchResult();
        //1. return all query product
        HitsMetadata<Object> hits = response.hits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if(hits.hits()!=null && hits.hits().size() > 0){
            for (Hit<Object> hit : hits.hits()) {
                Object source = hit.source();
                SkuEsModel esModel = JSON.parseObject(JSON.toJSONString(source), SkuEsModel.class);
                if(!StringUtils.isEmpty(param.getKeyword())){
                    List<String> skuTitle = hit.highlight().get("skuTitle");
                    String s = skuTitle.get(0);
                    esModel.setSkuTitle(s);
                }
                esModels.add(esModel);
            }
        }
        result.setProduct(esModels);

        //2. all related attributes info
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        NestedAggregate attr_agg = response.aggregations().get("attr_agg").nested();
        LongTermsAggregate attr_id_agg = attr_agg.aggregations().get("attr_id_agg").lterms();
        for (LongTermsBucket bucket : attr_id_agg.buckets().array()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            // 1. get attr id
            long attrId = Long.parseLong(bucket.key());
            // 2. get attr name
            StringTermsAggregate attr_name_agg = bucket.aggregations().get("attr_name_agg").sterms();
            String attr_name = attr_name_agg.buckets().array().get(0).key();
            // 3. get related value for attr
            StringTermsAggregate attr_value_agg = bucket.aggregations().get("attr_value_agg").sterms();
            List<String> attrValues = attr_value_agg.buckets().array().stream().map(item -> {
                String key = item.key();
                return key;
            }).collect(Collectors.toList());

            //update attrVo
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attr_name);
            attrVo.setAttrValue(attrValues);
            attrVos.add(attrVo);
        }
        result.setAttrs(attrVos);


        //3. all related brands
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        LongTermsAggregate brand_agg = response.aggregations().get("brand_agg").lterms();
        for (LongTermsBucket bucket : brand_agg.buckets().array()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //brand id
            Long brandId = Long.parseLong(bucket.key());
            //brand name
            StringTermsAggregate brand_name_agg = bucket.aggregations().get("brand_name_agg").sterms();
            String brand_name = brand_name_agg.buckets().array().get(0).key();
            //brand image
            StringTermsAggregate brand_img_agg = bucket.aggregations().get("brand_img_agg").sterms();
            String brand_img = brand_img_agg.buckets().array().get(0).key();
            //update brandVo
            brandVo.setBrandId(brandId);
            brandVo.setBrandImg(brand_img);
            brandVo.setBrandName(brand_name);
            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //4. all related categories
        LongTermsAggregate catalog_agg = response.aggregations().get("catalog_agg").lterms();

        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        Buckets<LongTermsBucket> buckets = catalog_agg.buckets();
        for (LongTermsBucket bucket : buckets.array()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            String key = bucket.key();
            //update catalog id
            catalogVo.setCatalogId(Long.parseLong(key));
            //update catalog name
            StringTermsAggregate catalog_name_agg = bucket.aggregations().get("catalog_name_agg").sterms();
            String catalog_name = catalog_name_agg.buckets().array().get(0).key();
            catalogVo.setCatalogName(catalog_name);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //5. pageNum
        result.setPageNum(param.getPageNum());

        //6. all records
        long total = hits.total().value();
        result.setTotal(total);

        //7. all pages
        int totalPages = (int) (total % EsConstant.PRODUCT_PAGESIZE == 0 ? total/EsConstant.PRODUCT_PAGESIZE : (total/EsConstant.PRODUCT_PAGESIZE + 1));
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }

        result.setPageNavs(pageNavs);

        if(param.getAttrs() != null && param.getAttrs().size() >0){
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = productFeignService.attrInfo(Long.parseLong(s[0]));
                result.getAttrIds().add(Long.parseLong(s[0]));
                if (r.getCode() == 0) {
                    AttrResponseVo data = r.getData("attr", new TypeReference<AttrResponseVo>() {
                    });
                    navVo.setNavName(data.getAttrName());
                } else {
                    navVo.setNavName(s[0]);
                }

                String replace = replaceQureyString(param, attr, "attrs");
                navVo.setLink("http://search.petchill.com/list.html?" + replace);
                return navVo;
            }).collect(Collectors.toList());



            result.setNavs(collect);
        }

        if(param.getBrandId()!=null && param.getBrandId().size() >0){
            List<SearchResult.NavVo> navs = result.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("Brand");
            R r = productFeignService.brandsInfo(param.getBrandId());
            if(r.getCode() ==0){
                List<BrandVo> brand = r.getData("brand", new TypeReference<List<BrandVo>>() {
                });

                StringBuffer stringBuffer = new StringBuffer();
                String replace = "";
                for (BrandVo brandVo : brand) {
                    stringBuffer.append(brandVo.getName()+";");
                    replace = replaceQureyString(param, brandVo.getBrandId() + "", "brandId");
                }
                navVo.setNavValue(stringBuffer.toString());
                navVo.setLink("http://search.petchill.com/list.html?" + replace);
            }
            navs.add(navVo);
        }
        return result;
    }

    private static String replaceQureyString(SearchParam param, String attr, String key) {
        String encode = null;
        try {
            encode = URLEncoder.encode(attr, "UTF-8");
            //browser handle the encoding diff from java lib
            encode = encode.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String replace = param.get_queryString().replace("&" + key +"=" + encode, "");
        return replace;
    }

    /***
     * prepare search request query
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(EsConstant.PRODUCT_INDEX);

         BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        /**
         * 查询：模糊匹配，过滤（按照属性，分类，品牌，价格区间，库存）
         */

        //1.1 must-模糊匹配
        if(!StringUtils.isEmpty(param.getKeyword())){

            boolQuery.must(m-> m
                            .fuzzy(v-> v
                                    .field("skuTitle").value(param.getKeyword())));
        }
        //1.2 bool - filter - catalogLevel3 id
        if(param.getCatalog3Id() != null){
            boolQuery.filter(f->f
                    .term(t->t
                            .field("catalogId").value(param.getCatalog3Id())));
        }
        //1.2 bool - filter - brandId
        if(param.getBrandId() != null && param.getBrandId().size() > 0){
            TermsQueryField ids = new TermsQueryField.Builder()
                    .value(param.getBrandId().stream().map(FieldValue::of).collect(Collectors.toList()))
                    .build();
            boolQuery.filter(f->f.terms(t->t.field("brandId").terms(ids)));
        }
        //1.2 bool - filter - by properties
        if(param.getAttrs() != null && param.getAttrs().size() > 0){

            //attrs=1_5寸:8寸&2_16G:8G
            for (String attrStr : param.getAttrs()) {
                NestedQuery.Builder nested = new NestedQuery.Builder();
                BoolQuery.Builder nestedboolQuery = new BoolQuery.Builder();

                //attr=1_5寸:8寸
                String[] s = attrStr.split("_");
                String attrId = s[0]; //search attribute id
                String[] attrValues = s[1].split(":"); // search attribute values
                nestedboolQuery.must(m->m
                        .term(t->t
                                .field("attrs.attrId").value(attrId)));

                TermsQueryField attrs = new TermsQueryField.Builder()
                        .value(Arrays.stream(attrValues).map(FieldValue::of).collect(Collectors.toList()))
                        .build();
                nestedboolQuery.must(m->m
                        .terms(t->t
                                .field("attrs.attrValue").terms(attrs)));
                //each attr need to be one nested search
                nested.path("attrs").scoreMode(ChildScoreMode.None).query(q->q.bool(nestedboolQuery.build()));
                boolQuery.filter(f->f.nested(nested.build()));
            }
        }
        //1.2 bool - filter - hasstock
        if(param.getHasStock() != null){
            boolQuery.filter(f->f
                    .term(t->t.field("hasStock")
                            .value(param.getHasStock() == 1)));
        }
        //1.2 bool - filter - by price
        if(!StringUtils.isEmpty(param.getSkuPrice())){
            RangeQuery.Builder rangeQuery = new RangeQuery.Builder().field("skuPrice");

            String[] s = param.getSkuPrice().split("_");
            if(s.length == 2 && !StringUtils.isEmpty(s[0]) && !StringUtils.isEmpty(s[1])){
                rangeQuery.gte(JsonData.of(s[0])).lte(JsonData.of(s[1]));
            }else {
                if(param.getSkuPrice().startsWith("_")){
                    rangeQuery.lte(JsonData.of(s[1]));
                }
                if(param.getSkuPrice().endsWith("_")){
                    rangeQuery.gte(JsonData.of(s[0]));
                }
            }

            boolQuery.filter(q->q.range(rangeQuery.build()));
        }

        builder.query(q->q.bool(boolQuery.build()));
        /**
         * 排序，分页，高亮
         */
        //2.1 order
        if(!StringUtils.isEmpty(param.getSort())){
            //sort=hotScore_asc/desc
            String sort = param.getSort();
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.Asc : SortOrder.Desc;
            builder.sort(st->st
                    .field(f->f
                            .field(s[0])
                            .order(order)));
        }
        //2.2 page
        builder.from((param.getPageNum() - 1)*EsConstant.PRODUCT_PAGESIZE);
        builder.size(EsConstant.PRODUCT_PAGESIZE);
//
        //2.3 highlight
        if(!StringUtils.isEmpty(param.getKeyword())){
            builder.highlight(h -> h
                    .fields("skuTitle", f->f
                            .preTags("<b style='color:red'>")
                            .postTags("</b>")));
        }
        /**
         * 聚合分析
         */
//        SearchResponse<Test> response11 = client.search(s -> s
//                        .index("newapi")
//                        .size(100)
//                        .aggregations("ageGroup", a -> a
//                                .terms(t -> t
//                                        .field("age")
//                                )
//                        )
//                , Test.class);
        //brand agg
        builder.aggregations("brand_agg", a->a
                .terms(t->t
                        .field("brandId").size(10))
                .aggregations("brand_name_agg", b->b.terms(t1->t1.field("brandName").size(1)))
                .aggregations("brand_img_agg", c->c.terms(t1->t1.field("brandImg").size(1)))
        );


        //catalog_agg
        builder.aggregations("catalog_agg", a->a
                .terms(t->t
                        .field("catalogId").size(10))
                .aggregations("catalog_name_agg", b->b.terms(t1->t1.field("catalogName").size(1)))
        );

        //attr_agg
        builder.aggregations("attr_agg", a->a
                .nested(n->n.path("attrs"))
                .aggregations("attr_id_agg", a1->a1
                        .terms(t1->t1.field("attrs.attrId").size(10))
                        .aggregations("attr_name_agg", a2->a2
                                .terms(t2->t2
                                        .field("attrs.attrName").size(1)))
                        .aggregations("attr_value_agg", a3->a3
                                .terms(t3->t3
                                        .field("attrs.attrValue").size(50)))
                )
        );



        System.out.println(builder);
        return builder.build();
    }
}
