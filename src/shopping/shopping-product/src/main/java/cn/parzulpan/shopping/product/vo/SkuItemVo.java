package cn.parzulpan.shopping.product.vo;

import cn.parzulpan.shopping.product.entity.SkuImagesEntity;
import cn.parzulpan.shopping.product.entity.SkuInfoEntity;
import cn.parzulpan.shopping.product.entity.SpuInfoDescEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.vo
 * @desc
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SkuItemVo {
    /** sku 基本信息 pms_sku_info */
    SkuInfoEntity info;
    /** 是否有货 */
    Boolean hasStock = true;
    /** sku 图片信息 pms_sku_images */
    List<SkuImagesEntity> images;
    /** spu 销售属性组合 pms_sku_info pms_sku_sale_attr_value */
    List<SkuItemSaleAttrVo> saleAttrs;
    /** spu 信息介绍 pms_spu_info_desc */
    SpuInfoDescEntity desc;
    /** spu 规格参数信息 */
    List<SpuItemAttrGroupVo> groupAttrs;
}
