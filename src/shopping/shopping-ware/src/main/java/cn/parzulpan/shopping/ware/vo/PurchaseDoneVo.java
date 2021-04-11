package cn.parzulpan.shopping.ware.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.ware.vo
 * @desc
 */

@Data
public class PurchaseDoneVo {
    /**
     *  id: 123,//采购单id
     *  items: [{itemId:1,status:4,reason:""}]//完成/失败的需求详情
     */
    @NotNull
    private Long id;
    private List<PurchaseDoneItemVo> items;

}
