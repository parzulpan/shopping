package cn.parzulpan.shopping.search.vo;

import cn.parzulpan.common.to.es.SkuEsModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.vo
 * @desc 检索结果
 */

@Data
public class SearchResult {
    /**查询到的所有商品信息*/
    private List<SkuEsModel> products;

    /**当前页码*/
    private Integer pageNum;

    /**总记录数*/
    private Long total;

    /**总页数*/
    private Integer totalPages;

    /**当前查询到的结果，所有涉及到的品牌*/
    private List<BrandVo> brands;

    /**当前查询到的结果，所有涉及到的分类*/
    private List<CatalogVo> catalogs;

    /**当前查询到的结果，所有涉及到的所有属性*/
    private List<AttrVo> attrs;

    /**导航页*/
    private List<Integer> pageNavs;

    /**导航数据*/
    private List<NavVo> navs = new ArrayList<>();

    /**判断当前 Id 是否被使用*/
    private List<Long> attrIds = new ArrayList<>();

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class NavVo {
        private String name;
        private String navValue;
        private String link;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class CatalogVo {
        private Long catalogId;
        private String catalogName;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }
}
