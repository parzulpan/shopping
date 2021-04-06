package cn.parzulpan.shopping.product.feign;

import cn.parzulpan.common.to.SkuReductionTo;
import cn.parzulpan.common.to.SpuBoundTo;
import cn.parzulpan.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.feign
 * @desc 优惠券查询远程服务
 */

@FeignClient("shopping-coupon")
public interface CouponFeignService {

    /**
     *     @PostMapping("/save")
     *     public R save(@RequestBody SpuBoundsEntity spuBounds)
     *
     * 注意：public R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);
     * 1. @RequestBody 将这个对象转为 json
     * 2. 找到 shopping-coupon 服务，给 /coupon/spubounds/save 发送请求，并且是将上一步转的 json 放到请求体里发送请求
     * 3. 对方服务收到请求，请求体里有 json 数据，
     * @RequestBody SpuBoundsEntity spuBounds 会将请求体的 json 转为 SpuBoundsEntity
     *
     * 结论：只要 json 数据模型是兼容的，双方服务无需使用同一个 To【（Transfer Object） 即不同的应用程序之间传输的对象】
     */
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/saveInfo")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);
}
