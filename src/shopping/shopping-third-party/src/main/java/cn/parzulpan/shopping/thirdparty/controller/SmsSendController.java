package cn.parzulpan.shopping.thirdparty.controller;

import cn.parzulpan.common.exception.BizCodeEnum;
import cn.parzulpan.common.utils.R;
import cn.parzulpan.shopping.thirdparty.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.auth.controller
 * @desc
 */

@RefreshScope
@RestController
public class SmsSendController {

    @Autowired
    SmsComponent smsComponent;

    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phoneNumber") String phoneNumber,
                      @RequestParam("templateParam") String templateParam) {
        try {
            if ("OK".equalsIgnoreCase(smsComponent.sendSmsCode(phoneNumber, templateParam))) {
                return R.ok();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.error(BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getMsg());
    }
}
