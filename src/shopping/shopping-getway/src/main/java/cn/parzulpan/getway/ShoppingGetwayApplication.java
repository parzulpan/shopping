package cn.parzulpan.getway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 使用网关
 *
 * 1. 开启服务注册发现
 *    可以排除数据源的配置
 * @author parzulpan
 */

@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class ShoppingGetwayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingGetwayApplication.class, args);
    }

}
