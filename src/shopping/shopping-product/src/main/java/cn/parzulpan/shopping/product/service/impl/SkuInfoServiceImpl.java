package cn.parzulpan.shopping.product.service.impl;

import cn.parzulpan.shopping.product.entity.SkuImagesEntity;
import cn.parzulpan.shopping.product.entity.SpuInfoDescEntity;
import cn.parzulpan.shopping.product.service.*;
import cn.parzulpan.shopping.product.vo.AttrGroupWithAttrsVo;
import cn.parzulpan.shopping.product.vo.SkuItemSaleAttrVo;
import cn.parzulpan.shopping.product.vo.SkuItemVo;
import cn.parzulpan.shopping.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.common.utils.Query;

import cn.parzulpan.shopping.product.dao.SkuInfoDao;
import cn.parzulpan.shopping.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveBaseSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        // key: 华为
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        // catelogId: 225
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        // brandId: 5
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        // min: 1000
        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min) && !"0".equalsIgnoreCase(min)) {
            // 大于等于
            wrapper.ge("price", min);
        }

        // max: 10000
        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max) && !"0".equalsIgnoreCase(max)) {
            try {
                BigDecimal bigDecimal = new BigDecimal(max);
                if (bigDecimal.compareTo(new BigDecimal("0")) == 1) {
                    // 小于等于
                    wrapper.le("price", max);
                }
            } catch (Exception e) {
                throw e;
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        return this.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
    }

    public SkuItemVo itemOld(Long skuId) {
        // 获取 sku 基本信息 pms_sku_info
        SkuInfoEntity info = getById(skuId);

        // 获取 sku 图片信息 pms_sku_images
        List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);

        // 获取 spu 销售属性组合 pms_sku_info pms_sku_sale_attr_value
        Long spuId = info.getSpuId();
        List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrBySpuId(spuId);

        // 获取 spu 信息介绍 pms_spu_info_desc
        SpuInfoDescEntity desc = spuInfoDescService.getById(spuId);

        // 获取 spu 规格参数信息
        Long catalogId = info.getCatalogId();
        List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(spuId, catalogId);


        SkuItemVo skuItemVo = new SkuItemVo(info, true, images, saleAttrs, desc, groupAttrs);
        return skuItemVo;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        // 使用异步编排，1 与 2 是没有关系的，3 4 5 必须依赖 1 的结果
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1. 获取 sku 基本信息 pms_sku_info
            SkuInfoEntity info = getById(skuId);
            skuItemVo.setInfo(info);
            return info;
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrsFuture = infoFuture.thenAcceptAsync((info) -> {
            // 3. 获取 spu 销售属性组合 pms_sku_info pms_sku_sale_attr_value
            List<SkuItemSaleAttrVo> saleAttrs = skuSaleAttrValueService.getSaleAttrBySpuId(info.getSpuId());
            skuItemVo.setSaleAttrs(saleAttrs);
        }, threadPoolExecutor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync((info) -> {
            // 4. 获取 spu 信息介绍 pms_spu_info_desc
            SpuInfoDescEntity desc = spuInfoDescService.getById(info.getSpuId());
            skuItemVo.setDesc(desc);
        }, threadPoolExecutor);

        CompletableFuture<Void> groupAttrsFuture = infoFuture.thenAcceptAsync((info) -> {
            // 5. 获取 spu 规格参数信息
            List<SpuItemAttrGroupVo> groupAttrs = attrGroupService.getAttrGroupWithAttrsBySpuId(info.getSpuId(), info.getCatalogId());
            skuItemVo.setGroupAttrs(groupAttrs);
        }, threadPoolExecutor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            // 2. 获取 sku 图片信息 pms_sku_images
            List<SkuImagesEntity> images = skuImagesService.getImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPoolExecutor);

        CompletableFuture.allOf(saleAttrsFuture, descFuture, groupAttrsFuture, imagesFuture).get();

        return skuItemVo;
    }

}