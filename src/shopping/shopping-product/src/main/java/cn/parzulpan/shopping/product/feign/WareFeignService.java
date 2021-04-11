package cn.parzulpan.shopping.product.feign;

import cn.parzulpan.common.to.SkuHasStockVo;
import cn.parzulpan.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.feign
 * @desc 库存查询远程服务
 */

@FeignClient("shopping-ware")
public interface WareFeignService {

    /**
     * 返回值通常有三种情况：
     * 1. R 设计的时候可以加上泛型，推荐使用
     * 2. 直接返回我们想要的结果
     * 3. 自己封装解析结果
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
