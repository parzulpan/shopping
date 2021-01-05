package cn.parzulpan.shopping.member.feign;

import cn.parzulpan.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author : parzulpan
 * @Time : 2021-01
 * @Desc : 优惠券查询远程服务
 */

@FeignClient("shopping-coupon")
public interface CouponFeignService {

    @RequestMapping("/coupon/coupon/member/list")
    public R memberCoupons();
}
