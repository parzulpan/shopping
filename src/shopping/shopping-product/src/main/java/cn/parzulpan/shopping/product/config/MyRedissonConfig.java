package cn.parzulpan.shopping.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.config
 * @desc 自定义 Redisson 配置类
 */

@Configuration
public class MyRedissonConfig {

    /**
     * 对所有的 Redisson 的使用都是通过 RedissonClient 对象
     * @return
     * @throws IOException
     */
    @Bean(destroyMethod = "shutdown")
    RedissonClient redisson() throws IOException {
//        // 默认连接地址 127.0.0.1:6379
//        RedissonClient redisson = Redisson.create();

        // 1、创建配置
        Config config = new Config();
        // Redis url should start with redis:// or rediss:// (for SSL connection)
        config.useSingleServer().setAddress("redis://192.168.56.56:6379");

        // 2、根据 Config 创建出 RedissonClient 实例
        RedissonClient redisson = Redisson.create(config);

        return redisson;
    }
}

