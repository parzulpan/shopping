package cn.parzulpan.shopping.search.constant;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.constant
 * @desc 搜索微服务相关常量
 */

public class EsConstant {
    /**
     * SKU 数据在 ES 中的 索引
     */
    public static final String PRODUCT_INDEX = "shopping_product";

    /**
     * 分页数量
     */
    public static final Integer PRODUCT_PAGE_SIZE = 16;

    /**
     *
     */
    public static final String SEARCH_LINK = "http://search.shopping.parzulpan.cn/list.html";
}
