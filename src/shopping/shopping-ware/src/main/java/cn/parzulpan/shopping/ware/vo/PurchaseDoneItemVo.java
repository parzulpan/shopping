package cn.parzulpan.shopping.ware.vo;

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
public class PurchaseDoneItemVo {
    // // items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
    private Long itemId;
    private Integer status;
    private String reason;
}
