package cn.parzulpan.shopping.ware;

import cn.parzulpan.shopping.ware.dao.WareSkuDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShoppingWareApplicationTests {

    @Autowired
    WareSkuDao wareSkuDao;

    @Test
    void contextLoads() {
    }

    @Test
    void testWareSkuDao() {
        Long count = wareSkuDao.getSkuStock(1L);
        System.out.println(count);
    }

}
