package cn.parzulpan.shopping.product.web;

import cn.parzulpan.shopping.product.entity.CategoryEntity;
import cn.parzulpan.shopping.product.service.CategoryService;
import cn.parzulpan.shopping.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author parzulpan
 * @version 1.0
 * @date 2021-04
 * @project shopping
 * @package cn.parzulpan.shopping.product.web
 * @desc
 */

@Controller
public class IndexController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        model.addAttribute("categorys", categoryEntities);

        // 视图解析器进行拼串
        // classpath:/templates(前缀) + 返回值 + .html(后缀)
        return "index";
    }

    /**
     * index/catalog.json
     */
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    /**
     * JMeter 压测 简单服务
     */
    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    /**
     * Redisson 可重入锁（Reentrant Lock）的简单接口测试
     */
    @ResponseBody
    @GetMapping("/hello/redisson")
    public String helloRedisson() {
        // 获取一把锁。只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");
        // 加锁。阻塞式等待，默认加的锁都是30s时间
        lock.lock();
        try {
            System.out.println("加锁成功，执行业务... " + Thread.currentThread().getName());
            // 业务执行需要 10s
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 解锁。假设当前服务执行时宕机，解锁代码没有运行
            System.out.println("释放锁... " + Thread.currentThread().getName());
            lock.unlock();
        }

        return "hello";
    }

    /**
     * Redisson 可重入锁（Reentrant Lock） 看门狗 的简单接口测试
     */
    @ResponseBody
    @GetMapping("/hello/redissonWatchdog")
    public String helloRedissonWatchdog() {
        // 获取一把锁。只要锁的名字一样，就是同一把锁
        RLock lock = redissonClient.getLock("my-lock");
        // 加锁。加锁以后10秒钟自动解锁
        lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("加锁成功，执行业务... " + Thread.currentThread().getName());
            // 业务执行需要 30s
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 解锁。假设当前服务执行时宕机，解锁代码没有运行
            System.out.println("释放锁... " + Thread.currentThread().getName());
            lock.unlock();
        }

        return "hello";
    }

    /**
     * Redisson 读写锁 ReadWriteLock 简单接口测试
     * 写入数据
     */
    @ResponseBody
    @GetMapping("/hello/redisson/writeLock")
    public String helloRedissonWriteLock() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock wLock = readWriteLock.writeLock();
        try {
            // 写入数据加写锁
            wLock.lock();
            s = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set("writeValue", s);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            wLock.unlock();
        }

        return s;
    }

    /**
     * Redisson 读写锁 ReadWriteLock 简单接口测试
     * 读取数据
     */
    @ResponseBody
    @GetMapping("/hello/redisson/readLock")
    public String helloRedissonReadLock() {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = readWriteLock.readLock();
        try {
            // 读取数据加读锁
            rLock.lock();
            s = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }

        return s;
    }

    /**
     * Redisson 闭锁 CountDownLatch 简单接口测试
     * 模拟一个放假锁门的场景。学校一共 5 个班，只有等 5 个班都没人了才可以锁学校大门。
     * 锁门方法
     */
    @ResponseBody
    @GetMapping("/hello/redisson/CDL/lockDoor")
    public String helloRedissonCDLLockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();

        return "已锁门";
    }

    /**
     * Redisson 闭锁 CountDownLatch 简单接口测试
     * 模拟一个放假锁门的场景。学校一共 5 个班，只有等 5 个班都没人了才可以锁学校大门。
     * 班级全部人离开
     */
    @ResponseBody
    @GetMapping("/hello/redisson/CDL/go/{id}")
    public String helloRedissonCDLGo(@PathVariable("id") Long id) {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();

        return id + " 班级全部人离开";
    }

    /**
     * Redisson 信号量 Semaphore 简单接口测试
     * 模拟一个车库停车的场景。5 个车位，同时只能有 5 辆车停，只有有车位了才能停车。
     * 车库停车
     */
    @ResponseBody
    @GetMapping("/hello/redisson/Semaphore/Park")
    public String helloRedissonSemaphorePark() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.acquire(5);

        return "车库停车成功";
    }

    /**
     * Redisson 信号量 Semaphore 简单接口测试
     * 模拟一个车库停车的场景。5 个车位，同时只能有 5 辆车停，只有有车位了才能停车。
     * 车库的车位上的车离开
     */
    @ResponseBody
    @GetMapping("/hello/redisson/Semaphore/Leave")
    public String helloRedissonSemaphoreLeave(){
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();

        return "车库的车位上的车离开";
    }

}
