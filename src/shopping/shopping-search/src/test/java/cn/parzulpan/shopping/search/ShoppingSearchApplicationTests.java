package cn.parzulpan.shopping.search;

import cn.parzulpan.shopping.search.config.ShoppingElasticsearchConfig;
import com.google.gson.Gson;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
class ShoppingSearchApplicationTests {

    @Autowired
    RestHighLevelClient client;

    @Test
    void contextLoads() {

    }

    @Test
    void testRestClient() {
        System.out.println(client);
    }

    @Data
    static class User {
        private String userName;
        private Integer age;
        private String gender;
    }

    @Data
    static class Account {
        private int accountNumber;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    /**
     * ES 保存数据
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.x/java-rest-high-create-index.html
     * 保存方式分为同步和异步
     */
    @Test
    void indexData() throws IOException {
        // 设置索引
        IndexRequest users = new IndexRequest("users");
        users.id("2");

        //设置要保存的内容，指定数据和类型
        // 方式一
//        users.source("userName", "zhang", "age", 18, "gender", "男");
        // 方式二
        User user = new User();
        user.setUserName("wang");
        user.setAge(20);
        user.setGender("女");
        Gson gson = new Gson();
        String userJson = gson.toJson(user);
        users.source(userJson, XContentType.JSON);

        // 执行创建索引和保存数据
        IndexResponse index = client.index(users, ShoppingElasticsearchConfig.COMMON_OPTIONS);

        System.out.println(index);
    }

    /**
     * ES 获取数据
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.x/java-rest-high-search.html
     * 搜索 address 中包含 mill 的所有人的年龄分布以及平均年龄
     * GET /bank/_search
     * {
     *   "query": { # 查询出包含 mill 的
     *     "match": {
     *       "address": "Mill"
     *     }
     *   },
     *   "aggs": { # 基于查询聚合
     *     "ageAgg": {  # 第一个聚合，聚合的名字，可以随便起
     *       "terms": { # 看值的可能性分布
     *         "field": "age",
     *         "size": 10
     *       }
     *     },
     *     "ageAvg": {  # 第二个聚合
     *       "avg": { # 看 age 值的平均
     *         "field": "age"
     *       }
     *     },
     *     "balanceAvg": { # 第三个聚合
     *       "avg": { # 看 balance 的平均
     *         "field": "balance"
     *       }
     *     }
     *   },
     *   "size": 0  # 不看详情
     * }
     */
    @Test
    void find() throws IOException {
        // 1. 创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 构造检索条件
//        searchSourceBuilder.query();
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
//        searchSourceBuilder.aggregation();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
        // 构建第一个聚合条件：看值的可能性分布
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        searchSourceBuilder.aggregation(ageAgg);
        // 构建第二个聚合条件：看 age 值的平均
        AvgAggregationBuilder ageAvg = AggregationBuilders.avg("ageAvg").field("age");
        searchSourceBuilder.aggregation(ageAvg);
        // 构建第三个聚合条件：看 balance 的平均
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);
        // 不看详情
//        searchSourceBuilder.size(0);

        System.out.println("searchSourceBuilder " + searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);

        // 2. 执行检索
        SearchResponse response = client.search(searchRequest, ShoppingElasticsearchConfig.COMMON_OPTIONS);

        // 3. 分析响应结果
        System.out.println("response " + response.toString());
        // 3.1 将响应结果转换为 Bean
        SearchHits hits = response.getHits();
        SearchHit[] hits1 = hits.getHits();
        Gson gson = new Gson();
        for (SearchHit hit: hits1) {
            System.out.println("id: " + hit.getId());
            System.out.println("index: " + hit.getIndex());
            String sourceAsString = hit.getSourceAsString();
            System.out.println("sourceAsString: " + sourceAsString);
            System.out.println("Account: " + gson.fromJson(sourceAsString, Account.class));
        }
        // 3.2 获取检索到的分析信息
        Aggregations aggregations = response.getAggregations();
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            System.out.println("ageAgg: " + bucket.getKeyAsString() + " => " + bucket.getDocCount());
        }
        Avg ageAvg1 = aggregations.get("ageAvg");
        System.out.println("ageAvg: " + ageAvg1.getValue());
        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("balanceAvg: " + balanceAvg1.getValue());
    }

}
