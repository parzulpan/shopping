package cn.parzulpan.shopping.thirdparty;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class ShoppingThirdPartyApplicationTests {

    @Autowired
    OSSClient ossClient;

    @Test
    void contextLoads() {
    }

    // OSS 方式一
    @Test
    void testOSSUpload() throws FileNotFoundException {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = "xx";
        String accessKeySecret = "xx";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        InputStream inputStream = new FileInputStream("D:\\Dev\\Code\\OpenSource\\shopping\\assets\\pics\\0d40c24b264aa511.jpg");
        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("shopping-admin-oss", "OSS方式一。测试.jpg", inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();

        System.out.println("上传完成...");
    }

    // OSS 方式二，推荐使用
    @Test
    void testOSSUpload2() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream("D:\\Dev\\Code\\OpenSource\\shopping\\assets\\pics\\0d40c24b264aa511.jpg");
        ossClient.putObject("shopping-admin-oss", "OSS方式二，推荐使用。测试.jpg", inputStream);
        ossClient.shutdown();

        System.out.println("上传完成...");
    }
}
