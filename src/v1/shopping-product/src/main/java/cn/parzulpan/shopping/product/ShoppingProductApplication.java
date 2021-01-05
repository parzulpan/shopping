package cn.parzulpan.shopping.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1. 整合 MyBatis-Plus
 *   1.1 导入依赖
 *   1.2 配置
 *     1.2.1 配置数据源
 *       1.2.1.1 导入数据库驱动
 *       1.2.1.2 在配置文件 配置数据源相关信息
 *     1.2.2 配置 MyBatis-Plus
 *       1.2.2.1 使用 @MapperScan 扫描 DAO
 *       1.2.1.2 在配置文件 配置映射文件地址
 */

@EnableDiscoveryClient
@MapperScan("cn.parzulpan.shopping.product.dao")
@SpringBootApplication
public class ShoppingProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingProductApplication.class, args);
    }

}