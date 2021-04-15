package cn.parzulpan.shopping.product;

import cn.parzulpan.shopping.product.entity.BrandEntity;
import cn.parzulpan.shopping.product.service.BrandService;
import cn.parzulpan.shopping.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@SpringBootTest
class ShoppingProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    void contextLoads() {

    }

    @Test
    void testFindPath() {
        Long[] catelogPath = categoryService.findCatelogPath(225L);
        log.info("完整路径：{}", Arrays.asList(catelogPath));
    }

    @Test
    void testBrandSave() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setName("华为");
        brandEntity.setDescript("这是华为系列品牌");
        brandService.save(brandEntity);
        System.out.println("保存成功...");
    }

    @Test
    void testBrandUpdate() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setName("华为");
        brandEntity.setDescript("这是更新的华为系列品牌");
        brandService.updateById(brandEntity);
    }

    @Test
    void testBrandList() {
        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach(System.out::println);
    }

    @Test
    void testRedis() {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set("hello", "word"+ UUID.randomUUID().toString());
        String hello = ops.get("hello");
        System.out.println(hello);
    }
}
