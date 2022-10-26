package com.alexhong.petchill.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import com.alexhong.common.to.es.SkuEsModel;
import com.alexhong.petchill.search.constant.EsConstant;
import com.alexhong.petchill.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {


    @Autowired
    ElasticsearchClient elasticsearchClient;
    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {

        //save data to es
        //1. build index in es, and mapping
        //2. save data to es
        List<BulkOperation> bulkOperations = new ArrayList<>();
        // add data to bulkOperations
        for(SkuEsModel model: skuEsModels){
            bulkOperations.add(new BulkOperation.Builder().create(d-> d.document(model).id(model.getSkuId().toString()).index(EsConstant.PRODUCT_INDEX)).build());
        }
        // insert data to es
        BulkResponse response = elasticsearchClient.bulk(e->e.index(EsConstant.PRODUCT_INDEX).operations(bulkOperations));
        boolean errors = response.errors();
        List<String> collect = response.items().stream().map(item -> item.id()).collect(Collectors.toList());
        if(errors){
            log.error("Update sku to es error, {}", collect);
        }else {
            log.error("Upload sku to es successfully, {}", collect);
        }

        return errors;
    }
}
