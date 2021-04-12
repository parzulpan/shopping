package cn.parzulpan.shopping.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.vo
 * @desc 二级分类
 */

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Catelog2Vo {
    /**
     * 一级分类 id
     */
    private String catalog1Id;
    /**
     * 三级子分类
     */
    private List<Catelog3Vo> catalog3List;
    private String id;
    private String name;

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class Catelog3Vo {
        /**
         * 二级分类 id
         */
        private String catalog2Id;
        private String id;
        private String name;
    }
}
