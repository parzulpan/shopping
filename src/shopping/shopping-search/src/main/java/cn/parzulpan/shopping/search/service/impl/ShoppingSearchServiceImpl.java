package cn.parzulpan.shopping.search.service.impl;

import cn.parzulpan.common.to.es.SkuEsModel;
import cn.parzulpan.common.utils.R;
import cn.parzulpan.shopping.search.config.ShoppingElasticsearchConfig;
import cn.parzulpan.shopping.search.constant.EsConstant;
import cn.parzulpan.shopping.search.feign.ProductFeignService;
import cn.parzulpan.shopping.search.service.ShoppingSearchService;
import cn.parzulpan.shopping.search.vo.SearchParam;
import cn.parzulpan.shopping.search.vo.SearchResult;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.service.impl
 * @desc
 */

@Slf4j
@Service("shoppingSearchService")
public class ShoppingSearchServiceImpl implements ShoppingSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    /**
     * DSL 语句参考 src/main/resources/DSL.json
     * <p>
     * 包括模糊匹配，过滤（按照属性、分类、品牌、价格区间、库存等），排序，分页，高亮，聚合分析
     * <p>
     * 请求带来的参数是 SearchParam
     * 传给 ES 的参数是 SearchRequest
     * ES 返回结果是 SearchResponse
     * 把结果封装为 SearchResult
     */
    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        // 准备检索请求
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            // 执行检索请求
            SearchResponse searchResponse = client.search(searchRequest, ShoppingElasticsearchConfig.COMMON_OPTIONS);
            // 封装检索结果
            searchResult = buildSearchResult(searchParam, searchResponse);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResult;
    }

    /**
     * 封装检索结果
     */
    private SearchResult buildSearchResult(SearchParam searchParam, SearchResponse searchResponse) {
        SearchResult searchResult = new SearchResult();
        SearchHits hits = searchResponse.getHits();

        // 1. 封装查询到的商品信息
        if (hits.getHits() != null && hits.getHits().length > 0) {
            List<SkuEsModel> skuEsModels = new ArrayList<>();
            for (SearchHit hit : hits) {
                String sourceAsString = hit.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                // 设置高亮属性
                if (!StringUtils.isEmpty(searchParam.getKeyword())) {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                skuEsModels.add(skuEsModel);
            }
            searchResult.setProducts(skuEsModels);
        }

        // 2. 封装分页信息
        // 2.1 当前页码
        searchResult.setPageNum(searchParam.getPageNum());
        // 2.2 总记录数
        long total = hits.getTotalHits().value;
        searchResult.setTotal(total);
        // 2.3 总页数
        int totalPages = (int) total % EsConstant.PRODUCT_PAGE_SIZE == 0 ?
                (int) total / EsConstant.PRODUCT_PAGE_SIZE :
                (int) total / EsConstant.PRODUCT_PAGE_SIZE + 1;
        searchResult.setTotalPages(totalPages);
        // 2.4 导航页
        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPages; ++i) {
            pageNavs.add(i);
        }
        searchResult.setPageNavs(pageNavs);

        // 3. 所有涉及到的品牌
        Aggregations aggregations = searchResponse.getAggregations();
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        // ParsedLongTerms 用于接收 terms 聚合的结果，并且可以把 key 转化为 Long 类型的数据
        ParsedLongTerms brandAgg = aggregations.get("brandAgg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {
            // 3.1 brandId
            Long brandId = bucket.getKeyAsNumber().longValue();
            Aggregations subAggs = bucket.getAggregations();
            // 3.2 brandImg
            ParsedStringTerms brandImgAgg = subAggs.get("brandImgAgg");
            String brandImg = brandImgAgg.getBuckets().get(0).getKeyAsString();
            // 3.3 brandName
            ParsedStringTerms brandNameAgg = subAggs.get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();

            SearchResult.BrandVo brandVo = new SearchResult.BrandVo(brandId, brandName, brandImg);
            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);

        // 4. 所有涉及到的分类
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        ParsedLongTerms catalogAgg = aggregations.get("catalogAgg");
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            // 4.1 catalogId
            long catalogId = bucket.getKeyAsNumber().longValue();
            Aggregations subAggs = bucket.getAggregations();
            // 4.2 catalogName
            ParsedStringTerms catalogNameAgg = subAggs.get("catalogNameAgg");
            String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();

            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo(catalogId, catalogName);
            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);

        // 5. 所有涉及到的所有属性
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        ParsedNested parsedNested = aggregations.get("attrs");
        ParsedLongTerms attrIdAgg = parsedNested.getAggregations().get("attrIdAgg");
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            // 5.1 attrId
            long attrId = bucket.getKeyAsNumber().longValue();
            Aggregations subAggs = bucket.getAggregations();
            // 5.2 attrName
            ParsedStringTerms attrNameAgg = subAggs.get("attrNameAgg");
            String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
            // 5.3 attrValue
            ParsedStringTerms attrValueAgg = subAggs.get("attrValueAgg");
            List<String> attrValues = new ArrayList<>();
            for (Terms.Bucket attrValueAggBucket : attrValueAgg.getBuckets()) {
                String attrValue = attrValueAggBucket.getKeyAsString();
                attrValues.add(attrValue);
            }

            SearchResult.AttrVo attrVo = new SearchResult.AttrVo(attrId, attrName, attrValues);
            attrVos.add(attrVo);
        }
        searchResult.setAttrs(attrVos);

        // 6. 导航数据
        List<String> attrs = searchParam.getAttrs();
        if (attrs != null && attrs.size() > 0) {
            List<SearchResult.NavVo> navVos = attrs.stream().map(attr -> {
                String[] split = attr.split("_");
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                // 6.1 name
                try {
                    R info = productFeignService.info(Long.parseLong(split[0]));
                    if (info.getCode() == 0) {
                        ;
                    }
                } catch (Exception e) {
                    log.error("远程调用商品服务查询属性失败", e);
                }

                // 6.2 navValue
                navVo.setNavValue(split[1]);

                // 6.3 link
                String queryString = searchParam.getQueryString();
                String replace = queryString.replace("&attrs=" + attr, "")
                        .replace("attrs=" + attr + "&", "")
                        .replace("attrs=" + attr, "");
                navVo.setLink(EsConstant.SEARCH_LINK + (replace.isEmpty() ? "" : "?" + replace));
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVos);
        }

        return searchResult;
    }

    /**
     * 准备检索请求，DSL -> Java
     */
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        // 用于构建 DSL 语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 1. bool query 构建开启
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 1.1 bool must
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
        }
        // 1.2 bool filter
        // 1.2.1 catalogId
        if (searchParam.getCatalog3Id() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        // 1.2.2 brandId
        if (searchParam.getBrandId() != null && searchParam.getBrandId().size() > 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", searchParam.getBrandId()));
        }
        // 1.2.3 hasStock
        if (searchParam.getHasStock() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }
        // 1.2.4 skuPrice 三种情况：1_500 或 _500 或 500_
        RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
        if (!StringUtils.isEmpty(searchParam.getSkuPrice())) {
            String[] prices = searchParam.getSkuPrice().split("_");
            if (prices.length == 1) {
                if (searchParam.getSkuPrice().startsWith("_")) {
                    skuPrice.lte(Integer.parseInt(prices[0]));
                } else {
                    skuPrice.gte(Integer.parseInt(prices[0]));
                }
            } else if (prices.length == 2) {
                if (!prices[0].isEmpty()) {
                    skuPrice.gte(Integer.parseInt(prices[0]));
                }
                skuPrice.lte(Integer.parseInt(prices[1]));
            }
            boolQueryBuilder.filter(skuPrice);
        }
        // 1.2.5 attrs nested attrs=1_5寸:8寸&2_16G:8G
        List<String> attrs = searchParam.getAttrs();
        BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();
        if (attrs != null && attrs.size() > 0) {
            attrs.forEach(attr -> {
                String[] attrSplit = attr.split("_");
                boolQueryBuilder1.must(QueryBuilders.termQuery("attrs.attrId", attrSplit[0]));
                String[] attrValues = attrSplit[1].split(":");
                boolQueryBuilder1.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
            });
            NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("attrs", boolQueryBuilder1, ScoreMode.None);
            boolQueryBuilder.filter(nestedQueryBuilder);
        }
        // 1. bool query 构建结束
        searchSourceBuilder.query(boolQueryBuilder);

        // 2. sort
        if (!StringUtils.isEmpty(searchParam.getSort())) {
            String[] split = searchParam.getSort().split("_");
            searchSourceBuilder.sort(split[0],
                    "asc".equalsIgnoreCase(split[1]) ? SortOrder.ASC : SortOrder.DESC);
        }

        // 3. page
        searchSourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PRODUCT_PAGE_SIZE);
        searchSourceBuilder.size(EsConstant.PRODUCT_PAGE_SIZE);

        // 4. highlight
        if (!StringUtils.isEmpty(searchParam.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            searchSourceBuilder.highlighter(highlightBuilder);
        }

        // 5. aggs
        // 5.1 brandAgg
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brandAgg").field("brandId");
        TermsAggregationBuilder brandNameAgg = AggregationBuilders.terms("brandNameAgg").field("brandName");
        TermsAggregationBuilder brandImgAgg = AggregationBuilders.terms("brandImgAgg").field("brandImg");
        brandAgg.subAggregation(brandNameAgg);
        brandAgg.subAggregation(brandImgAgg);
        searchSourceBuilder.aggregation(brandAgg);
        // 5.2 catalogAgg
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalogAgg").field("catalogId");
        TermsAggregationBuilder catalogNameAgg = AggregationBuilders.terms("catalogNameAgg").field("catalogName");
        catalogAgg.subAggregation(catalogNameAgg);
        searchSourceBuilder.aggregation(catalogAgg);
        // 5.3 attrs
        NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder("attrs", "attrs");
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attrNameAgg").field("attrs.attrName");
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue");
        attrIdAgg.subAggregation(attrNameAgg);
        attrIdAgg.subAggregation(attrValueAgg);
        nestedAggregationBuilder.subAggregation(attrIdAgg);
        searchSourceBuilder.aggregation(nestedAggregationBuilder);

        log.debug("构建的 DSL 语句为 {}", searchSourceBuilder);

        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, searchSourceBuilder);
    }
}
