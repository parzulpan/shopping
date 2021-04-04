package cn.parzulpan.shopping.product.vo;

import lombok.Data;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.vo
 * @desc Attr的响应数据
 */

@Data
public class AttrRespVo extends AttrVo{
    /**
     * "catelogName": "手机/数码/手机", //所属分类名字
     * "groupName": "主体", //所属分组名字
     */
    private String catelogName;
    private String groupName;
}
