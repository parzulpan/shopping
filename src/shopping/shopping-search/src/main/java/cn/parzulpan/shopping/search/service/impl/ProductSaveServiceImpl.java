package cn.parzulpan.shopping.search.service.impl;

import cn.parzulpan.common.to.es.SkuEsModel;
import cn.parzulpan.shopping.search.config.ShoppingElasticsearchConfig;
import cn.parzulpan.shopping.search.constant.EsConstant;
import cn.parzulpan.shopping.search.service.ProductSaveService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
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
@Service("productSaveService")
public class ProductSaveServiceImpl implements ProductSaveService {
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        // 保存到 ES
        // 1. 给 ES 中 建立索引，建立好映射关系

        // 2. 给 ES 中保存这些数据
        // BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();
        Gson gson = new Gson();
        for (SkuEsModel model : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            String s = gson.toJson(model);
            indexRequest.source(s, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ShoppingElasticsearchConfig.COMMON_OPTIONS);

        // TODO 批量保存错误
        boolean b = bulk.hasFailures();
        if (b) {
            List<String> collect = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getId).collect(Collectors.toList());
            log.error("商品上架错误：{}", collect);
        }

        return !b;
    }
}
