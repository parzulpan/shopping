package cn.parzulpan.shopping.thirdparty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author parzulpan
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ShoppingThirdPartyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingThirdPartyApplication.class, args);
    }

}
