package cn.parzulpan.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.common.to.es
 * @desc SKU 的 ES 模型
 *
 * PUT product
 * {
 *     "mappings":{
 *         "properties": {
 *             "skuId":{ "type": "long" },
 *             "spuId":{ "type": "keyword" },  # 不可分词
 *             "skuTitle": {
 *                 "type": "text",
 *                 "analyzer": "ik_smart"  # 中文分词器
 *             },
 *             "skuPrice": { "type": "keyword" },  # 保证精度问题
 *             "skuImg"  : { "type": "keyword" },
 *             "saleCount":{ "type":"long" },
 *             "hasStock": { "type": "boolean" },
 *             "hotScore": { "type": "long"  },
 *             "brandId":  { "type": "long" },
 *             "catalogId": { "type": "long"  },
 *             "brandName": { "type": "keyword" },
 *             "brandImg":{
 *                 "type": "keyword",
 *             },
 *             "catalogName": { "type": "keyword" },
 *             "attrs": {
 *                 "type": "nested", # 防止扁平化处理
 *                 "properties": {
 *                     "attrId": { "type": "long" },
 *                     "attrName": {
 *                         "type": "keyword",
 *                     },
 *                     "attrValue": { "type": "keyword" }
 *                 }
 *             }
 *         }
 *     }
 * }
 *
 */

@Data
public class SkuEsModel {
    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;
    private Boolean hasStock;
    private Long hotScore;
    private Long brandId;
    private Long catalogId;
    private String brandName;
    private String brandImg;
    private String catalogName;
    private List<Attrs> attrs;

    @Data
    public static class Attrs {
        private Long attrId;
        private String attrName;
        private String attrValue;
    }

}
