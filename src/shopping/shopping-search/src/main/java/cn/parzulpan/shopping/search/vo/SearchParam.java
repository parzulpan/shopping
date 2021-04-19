package cn.parzulpan.shopping.search.vo;

import lombok.Data;

import java.util.List;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.vo
 * @desc 检索参数
 *
 * 全文检索：skuTitle（keyword）
 * 排序：saleCount（销量）、hotScore（热度分）、skuPrice（价格）
 * 过滤：hasStock、skuPrice（价格区间）、brandId、catalog3Id、attrs
 * 聚合：attrs
 *
 * 例子：catalog3Id=225&keyword=华为&sort=saleCount_asc&hasStock=0/1&brandId=25&brandId=30
 */

@Data
public class SearchParam {
    /**全文检索匹配关键字*/
    private String keyword;
    
    /**三级分类 Id*/
    private Long catalog3Id;
    
    /**排序条件 *_asc/desc*/
    private String sort;
    
    /**过滤条件 是否仅显示有货 0/1*/
    private Integer hasStock;
    
    /**过滤条件 价格区间 1_500/_500/500_*/
    private String skuPrice;
    
    /**过滤条件 品牌 Id，可多选*/
    private List<Long> brandId;
    
    /**过滤条件 按照属性进行筛选*/
    private List<String> attrs;
    
    /**页码*/
    private Integer pageNum = 1;
    
    /**原生的所有查询属性*/
    private String queryString;

}
