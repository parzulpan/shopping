package cn.parzulpan.shopping.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.config
 * @desc Elasticsearch 配置类
 */

@Configuration
public class ShoppingElasticsearchConfig {
    @Value("${es.httpHost}")
    private String httpHost;
    @Value("${es.port}")
    private int port;

    /** 请求测试项，比如 es 添加了安全访问规则，访问 es 需要添加一个安全头，就可以通过 requestOptions 设置
     * 官方建议把 requestOptions 创建成单实例
     */
    public static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        RestClientBuilder builder = null;
        // 可以指定多个 ES
        builder = RestClient.builder(new HttpHost(httpHost, port, "http"));
        return new RestHighLevelClient(builder);
    }

}
