package cn.parzulpan.shopping.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author parzulpan
 */
@EnableDiscoveryClient
@SpringBootApplication
public class ShoppingSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShoppingSearchApplication.class, args);
    }

}
