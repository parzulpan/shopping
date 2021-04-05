package cn.parzulpan.shopping.product.service.impl;

import cn.parzulpan.shopping.product.entity.*;
import cn.parzulpan.shopping.product.service.*;
import cn.parzulpan.shopping.product.vo.BaseAttrs;
import cn.parzulpan.shopping.product.vo.SpuSaveVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.common.utils.Query;

import cn.parzulpan.shopping.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;


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

        // 4. 保存 spu 的 规格参数 shopping_pms.pms_product_attr_value 积分信息 shopping_sms.sms_spu_bounds
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

        // 5. 保存当前 spu 对应的所有 sku 信息
        // 5.1 sku 的基本信息 shopping_pms.pms_sku_info
        

        // 5.2 sku 的图片信息 shopping_pms.pms_sku_images
        // 5.3 sku 的销售属性 shopping_pms.pms_sku_sale_attr_value
        // 5.4 sku 的积分、优惠、满减等信息 shopping_sms.sms_sku_bounds shopping_sms.sms_member_price shopping_sms.sms_sku_full_reduction shopping_sms.sms_sku_ladder
    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

}