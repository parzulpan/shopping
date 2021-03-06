package cn.parzulpan.shopping.product.vo;

import lombok.Data;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.vo
 * @desc
 */
@Data
public class AttrGroupRelationVo {
    // [{"attrId":1,"attrGroupId":2}]
    private Long attrId;
    private Long attrGroupId;
}
