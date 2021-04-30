package cn.parzulpan.shopping.auth.controller;

import cn.parzulpan.common.constant.AuthServerConstant;
import cn.parzulpan.common.exception.BizCodeEnum;
import cn.parzulpan.common.utils.R;
import cn.parzulpan.shopping.auth.feign.ThirdPartyFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.auth.controller
 * @desc
 */
@RestController
public class LoginController {

    @Autowired
    ThirdPartyFeignService thirdPartyFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phoneNumber") String phoneNumber) {
        // 接口防刷 用 redis 缓存数据
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phoneNumber);
        // 取出的数据不为空且当前时间在存储时间的 60s 以内，说明 60s 内该号码已经调用过，返回错误信息
        if (redisCode != null && redisCode.length() > 0) {
            long currTime = Long.parseLong(redisCode.split("_")[1]);
            if ((System.currentTimeMillis() - currTime) < 60 * 1000) {
                return R.error(BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getMsg());
            }
        }

        // 生成并缓存验证码
        String templateParam =  UUID.randomUUID().toString().substring(0, 6);
        String code = templateParam + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phoneNumber, code, 10, TimeUnit.MINUTES);

        // 调用第三方短信服务
        thirdPartyFeignService.sendCode(phoneNumber, templateParam);
        return R.ok();
    }

}
