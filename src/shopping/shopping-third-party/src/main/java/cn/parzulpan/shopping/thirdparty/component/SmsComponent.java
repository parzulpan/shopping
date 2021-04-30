package cn.parzulpan.shopping.thirdparty.component;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.thirdparty.component
 * @desc 短信服务 SMS 组件
 */

@Component
@RefreshScope
public class SmsComponent {
    @Value("${spring.cloud.alicloud.access-key}")
    private String accessKeyId;

    @Value("${spring.cloud.alicloud.secret-key}")
    private String accessKeySecret;

    @Value("${spring.cloud.alicloud.sms.endpoint}")
    private String endpoint;

    @Value("${spring.cloud.alicloud.sms.sign-name}")
    private String signName;

    @Value("${spring.cloud.alicloud.sms.template-code}")
    private String templateCode;

    /**
     *
     * @param phoneNumber 接收短信的手机号码
     * @param templateParam 短信模板变量对应的实际值
     * @return 请求状态码，返回 OK 代表请求成功
     * @throws Exception sendSms
     */
    public String sendSmsCode(String phoneNumber, String templateParam) throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = endpoint;
        Client client = new Client(config);
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setPhoneNumbers(phoneNumber)
                .setSignName(signName)
                .setTemplateCode(templateCode)
                .setTemplateParam(templateParam);

        return client.sendSms(sendSmsRequest).getBody().code;
    }
}
