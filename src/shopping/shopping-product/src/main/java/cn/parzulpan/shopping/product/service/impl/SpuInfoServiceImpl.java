package cn.parzulpan.shopping.product.service.impl;

import cn.parzulpan.common.constant.ProductConstant;
import cn.parzulpan.common.to.SkuHasStockVo;
import cn.parzulpan.common.to.SkuReductionTo;
import cn.parzulpan.common.to.SpuBoundTo;
import cn.parzulpan.common.to.es.SkuEsModel;
import cn.parzulpan.common.utils.R;
import cn.parzulpan.shopping.product.entity.*;
import cn.parzulpan.shopping.product.feign.CouponFeignService;
import cn.parzulpan.shopping.product.feign.SearchFeignService;
import cn.parzulpan.shopping.product.feign.WareFeignService;
import cn.parzulpan.shopping.product.service.*;
import cn.parzulpan.shopping.product.vo.*;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.common.utils.Query;

import cn.parzulpan.shopping.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService saveDescSpuInfo;

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
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * TODO 其他高级处理，例如事务问题，服务不稳定问题
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {
        // 1. 保存 spu 基本信息 shopping_pms.pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(spuInfoEntity);

        // 2. 保存 spu 的描述图片 shopping_pms.pms_spu_info_desc
        List<String> decript = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        // TODO 这里没有考虑分布式的 id 自增
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", decript));
        saveDescSpuInfo.saveDescSpuInfo(spuInfoDescEntity);

        // 3. 保存 spu 的图片集 shopping_pms.pms_spu_images
        List<String> images = vo.getImages();
        spuImagesService.saveImages(spuInfoEntity.getId(), images);

        // 4. 保存 spu 的 规格参数 shopping_pms.pms_product_attr_value
        // 积分信息 shopping_sms.sms_spu_bounds
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> collect = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            productAttrValueEntity.setAttrId(attr.getAttrId());
            AttrEntity byId = attrService.getById(attr.getAttrId());
            productAttrValueEntity.setAttrName(byId.getAttrName());
            productAttrValueEntity.setAttrValue(attr.getAttrValues());
            productAttrValueEntity.setQuickShow(attr.getShowDesc());
            productAttrValueEntity.setSpuId(spuInfoEntity.getId());

            return productAttrValueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveProductAttr(collect);

        Bounds bounds = vo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds, spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0) {
            log.error("远程保存 spu 的积分信息失败！");
        }

        // 5. 保存当前 spu 对应的所有 sku 信息
        // 5.1 sku 的基本信息 shopping_pms.pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            skus.forEach(item -> {
                //    private String skuName;
                //    private BigDecimal price;
                //    private String skuTitle;
                //    private String skuSubtitle;
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                String defaultImg = "";
                for (Images image: item.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImg = image.getImgUrl();
                    }
                }
                skuInfoEntity.setSkuDefaultImg(defaultImg);
                skuInfoService.saveBaseSkuInfo(skuInfoEntity);

                // 5.2 sku 的图片信息 shopping_pms.pms_sku_images
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntities = item.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    return skuImagesEntity;
                }).filter(entity -> {
                    // 没有图片路径的无需保存
                    // 返回 true 就是需要保存
                    return !StringUtils.isEmpty(entity.getImgUrl());
                }).collect(Collectors.toList());
                skuImagesService.saveBatch(imagesEntities);

                // 5.3 sku 的销售属性 shopping_pms.pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                // 5.4 sku 的积分、优惠、满减等信息
                // shopping_sms.sms_sku_bounds
                // shopping_sms.sms_member_price
                // shopping_sms.sms_sku_full_reduction
                // shopping_sms.sms_sku_ladder
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0) {
                        log.error("远程保存 sku 优惠信息失败！");
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

        // status: 1
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status) && !"0".equalsIgnoreCase(status)) {
            wrapper.and((w) -> {
                w.eq("publish_status", status);
            });
        }

        // key: 华为
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }

        // brandId: 5
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.and((w) -> {
                w.eq("brand_id", brandId);
            });
        }

        // catelogId: 225
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.and((w) -> {
                w.eq("catalog_id", catelogId);
            });
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {
        // 1. 查出当前 spuId 对应的所有 sku 信息
        List<SkuInfoEntity> skus = skuInfoService.getSkusBySpuId(spuId);

        // TODO 查询当前 SKU 的所有可以被检索规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListForSpu(spuId);
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrIds(attrIds);
        Set<Long> idSet = new HashSet<>(searchAttrIds);
        List<SkuEsModel.Attrs> collect = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item -> {
            SkuEsModel.Attrs attrs1 = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs1);
            return attrs1;
        }).collect(Collectors.toList());

        // TODO 发送远程调用，库存服务查询是否有库存
        Map<Long, Boolean> map = null;
        try {
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>() {
            };
            List<Long> skuIds = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
            R r = wareFeignService.getSkusHasStock(skuIds);
            // List<SkuHasStockVo> data = hasStock.getData();
            // 转成 map，查询更快
            map = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常 {}", e);
        }

        // 2. 封装每个 sku 的信息
        Map<Long, Boolean> finalMap = map;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            // 组装需要的数据
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, skuEsModel);
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());
            // TODO 发送远程调用，库存服务查询是否有库存
            if (finalMap == null) {
                skuEsModel.setHasStock(true);
            } else {
                skuEsModel.setHasStock(finalMap.get(sku.getSkuId()));
            }

            // TODO 热度评分，这里先默认为 0
            skuEsModel.setHotScore(0L);

            // TODO 查询品牌和分类的名字信息
            BrandEntity brand = brandService.getById(skuEsModel.getBrandId());
            skuEsModel.setBrandName(brand.getName());
            skuEsModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(skuEsModel.getBrandId());
            skuEsModel.setCatalogName(category.getName());

            // 设置检索属性
            skuEsModel.setAttrs(collect);

            return  skuEsModel;

        }).collect(Collectors.toList());

        // TODO 将数据发送给 ES 进行保存
        R r = searchFeignService.productStatusUp(upProducts);
        if ( r.getCode() == 0 ) {
            // 修改当前 spu 的状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            // 远程调用失败
            // TODO 重复调用问题？接口幂等性？重试机制？
            // Feign 调用流程
            /**
             * 1. 构造请求数据，将对象转为 json
             * 2. 发送请求并进行执行，执行成功会解码响应数据
             * 3. 执行请求会有重试机制
             *
             * 源码：
             *   @Override
             *   public Object invoke(Object[] argv) throws Throwable {
             *     RequestTemplate template = buildTemplateFromArgs.create(argv); // 1.
             *     Options options = findOptions(argv);
             *     Retryer retryer = this.retryer.clone();
             *     while (true) {
             *       try {
             *         return executeAndDecode(template, options); // 2.
             *       } catch (RetryableException e) {
             *         try {
             *           retryer.continueOrPropagate(e); // 3.
             *         } catch (RetryableException th) {
             *           Throwable cause = th.getCause();
             *           if (propagationPolicy == UNWRAP && cause != null) {
             *             throw cause;
             *           } else {
             *             throw th;
             *           }
             *         }
             *         if (logLevel != Logger.Level.NONE) {
             *           logger.logRetry(metadata.configKey(), logLevel);
             *         }
             *         continue;
             *       }
             *     }
             *   }
             */
        }

    }

}