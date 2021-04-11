package cn.parzulpan.common.to;

import lombok.Data;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.ware.vo
 * @desc
 */

@Data
public class SkuHasStockVo {
    /**
     * sku_id stock
     */
    private Long skuId;
    private Boolean hasStock;
}
