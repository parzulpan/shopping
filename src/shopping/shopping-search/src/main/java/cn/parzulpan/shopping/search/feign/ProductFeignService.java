package cn.parzulpan.shopping.search.feign;

import cn.parzulpan.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.search.feign
 * @desc 商品查询远程服务
 */

@FeignClient("shopping-product")
public interface ProductFeignService {

    /**
     * 注意：一共有两种实现方式
     * 1. 让所有请求过网关
     * 1.1 @FeignClient("shopping-getway") 是给 shopping-getway 所在的机器发送请求
     * 1.2 所以请求路径是：/api/product/skuinfo/info/{skuId}
     *
     * 2. 直接让后台指定服务处理
     * 2.1 @FeignClient("shopping-product")
     * 2.2 所以请求路径是：/product/skuinfo/info/{skuId}
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
