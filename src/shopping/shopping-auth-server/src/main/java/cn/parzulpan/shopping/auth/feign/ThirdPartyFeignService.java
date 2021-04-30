package cn.parzulpan.shopping.auth.feign;

import cn.parzulpan.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.auth.feign
 * @desc
 */

@FeignClient("shopping-third-party")
public interface ThirdPartyFeignService {

    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phoneNumber") String phoneNumber,
                      @RequestParam("templateParam") String templateParam);
}
