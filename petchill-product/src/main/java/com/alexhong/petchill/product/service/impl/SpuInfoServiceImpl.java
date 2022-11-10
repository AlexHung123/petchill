package com.alexhong.petchill.product.service.impl;

import com.alexhong.common.constant.ProductConstant;
import com.alexhong.common.to.SkuHasStockVo;
import com.alexhong.common.to.SkuReductionTo;
import com.alexhong.common.to.SpuBoundTo;
import com.alexhong.common.to.es.SkuEsModel;
import com.alexhong.common.utils.R;
import com.alexhong.petchill.product.entity.*;
import com.alexhong.petchill.product.feign.CouponFeignSrvice;
import com.alexhong.petchill.product.feign.SearchFeighService;
import com.alexhong.petchill.product.feign.WareFeignService;
import com.alexhong.petchill.product.service.*;
import com.alexhong.petchill.product.vo.*;
import com.qiniu.util.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.TypeReference;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.alexhong.common.utils.PageUtils;
import com.alexhong.common.utils.Query;

import com.alexhong.petchill.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignSrvice couponFeignSrvice;
    
    @Autowired
    BrandService brandService;
    
    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeighService searchFeighService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        //1、保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);
        //2、保存Spu的描述图片 pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);
        //3、保存spu的图片集 pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        //4、保存spu的规格参数;pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());

            AttrEntity id = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(id.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());

            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);
        //5、保存spu的积分信息；gulimall_sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignSrvice.saveSpuBounds(spuBoundTo);
        if(r.getCode() !=0 ){
            log.error("Remote saving spu bounds info failed");
        }

        //6、保存当前spu对应的所有sku信息；
            //6.1）、sku的基本信息；pms_sku_info
        List<Skus> skus = vo.getSkus();
        if(skus != null || skus.size() > 0){
            skus.forEach(item->{

                String defaultImg = "";
                for (Images image: item.getImages()){
                    if(image.getDefaultImg() == 1){
                        defaultImg = image.getImgUrl();
                    }
                }

                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imageEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity-> !StringUtils.isNullOrEmpty(entity.getImgUrl())).collect(Collectors.toList());
                //6.2）、sku的图片信息；pms_sku_image
                //TODO not need to save the image without url
                skuImagesService.saveBatch(imageEntities);
                //6.3）、sku的销售属性信息：pms_sku_sale_attr_value
                List<Attr> attrs = item.getAttr();
                List<SkuSaleAttrValueEntity> saleAttrs = attrs.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);

                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());

                skuSaleAttrValueService.saveBatch(saleAttrs);
                //6.4）、sku的优惠、满减等信息；gulimall_sms->sms_sku_ladder\sms_sku_full_reduction\sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if(skuReductionTo.getFullCount() >0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0"))==1){
                    R r1 = couponFeignSrvice.saveSkuReduction(skuReductionTo);
                    if(r1.getCode() != 0){
                        log.error("Remote saving spu reduction info failed");
                    }
                }
            });
        }





    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(!StringUtils.isNullOrEmpty(key)){
            wrapper.and(w->{
                w.eq("id", key).or().like("spu_name", key);
            });
        }
        String status = (String) params.get("status");
        if(!StringUtils.isNullOrEmpty(status)){
            wrapper.eq("publish_status", status);
        }
        String brandId = (String) params.get("brandId");
        if(!StringUtils.isNullOrEmpty(brandId) && !"0".equalsIgnoreCase(brandId)){
            wrapper.eq("brand_id", brandId);
        }
        String catelogId = (String) params.get("catelogId");
        if(!StringUtils.isNullOrEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)){
            wrapper.eq("catalog_id", catelogId);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {

        //1. get all related sku by spuId
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        //TODO 4. check and return all searchable properties by sku
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrlistforspu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(attr -> attr.getAttrId()).collect(Collectors.toList());

        List<Long> searchAttrIds = attrService.selectSearchAttr(attrIds);

        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> attrsList = baseAttrs.stream()
                .filter(item -> idSet.contains(item.getAttrId()))
                .map(item -> {
                    SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
                    BeanUtils.copyProperties(item, attrs1);
                    return attrs1;
                })
                .collect(Collectors.toList());

        //TODO 1. Check whether any stock in ware system(hasStock)
        Map<Long, Boolean> stockMap = null;
        try {
            R r = wareFeignService.getSkusHasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {};
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch (Exception e){
            log.error("ware service exception: reason {}", e);
        }

        //2. convert skuentity to skuEsModel
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            if(finalStockMap ==null){
                skuEsModel.setHasStock(true);
            }else {
                skuEsModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }
            //TODO 2. check hotScore
            skuEsModel.setHotScore(0L);
            //TODO 3. check brand name and category name info
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(skuEsModel.getCatalogId());
            skuEsModel.setCatalogName(category.getName());

            //set searchable attribute
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());


        //TODO: 5 send data to es for saving
        R r = searchFeighService.productStatusUp(upProducts);
        if(r.getCode() == 0){
            //TODO 6. update spu status
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else {
            //TODO 7. Retry mechanism? invoke repeatly?
        }
    }

}