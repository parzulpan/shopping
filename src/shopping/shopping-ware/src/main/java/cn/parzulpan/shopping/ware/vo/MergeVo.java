package cn.parzulpan.shopping.ware.vo;

import lombok.Data;

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
public class MergeVo {
    /**
     * {
     *       purchaseId: 1, //整单id
     *       items:[1,2,3,4] //合并项集合
     * }
     */
    private Long purchaseId;
    private List<Long> items;
}
