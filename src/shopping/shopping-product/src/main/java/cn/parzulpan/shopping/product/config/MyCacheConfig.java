package cn.parzulpan.shopping.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.config
 * @desc 自定义缓存配置类
 *
 * 使用自定义缓存配置类后，缓存配置文件中设置的属性将会失效，
 * 比如：spring.cache.redis.time-to-live=3600000 # 设置缓存存活时间
 * 所以需要另外在这里配置重新绑定。
 *
 * 1. 原来和配置文件绑定的配置类是这样子的：
 * @ConfigurationProperties(
 *     prefix = "spring.cache"
 * )
 * public class CacheProperties {}
 *
 * 2. 现在要让它生效：
 *   加上 @EnableConfigurationProperties(CacheProperties.class)  让 CacheProperties 的绑定生效
 *   注入 CacheProperties 或者在配置方法中加上 CacheProperties 参数
 *
 */

@EnableConfigurationProperties(CacheProperties.class)
@EnableCaching
@Configuration
public class MyCacheConfig {

//    @Autowired
//    CacheProperties cacheProperties;

    @Bean
    RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        // 链式
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

        // 设置 key 的序列化，使用 redis string
        config = config.serializeKeysWith(RedisSerializationContext
                .SerializationPair
                .fromSerializer(new StringRedisSerializer()));

        // 设置 value 的序列化，使用 fastjson
        //  /*GenericFastJsonRedisSerializer()*/
        config = config.serializeValuesWith(RedisSerializationContext
                .SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 获取 redis 配置
        CacheProperties.Redis redis = cacheProperties.getRedis();

        // 使配置文件中的所有配置都生效
        if (redis.getTimeToLive() != null) {
            config = config.entryTtl(redis.getTimeToLive());
        }
        if (redis.getKeyPrefix() != null) {
            config = config.prefixKeysWith(redis.getKeyPrefix());
            config = config.prefixCacheNameWith(redis.getKeyPrefix());
        }
        if (!redis.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redis.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }

        return config;

    }

}
