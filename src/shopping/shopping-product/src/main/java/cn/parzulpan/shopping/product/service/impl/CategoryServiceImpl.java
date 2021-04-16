package cn.parzulpan.shopping.product.service.impl;

import cn.parzulpan.shopping.product.service.CategoryBrandRelationService;
import cn.parzulpan.shopping.product.vo.Catelog2Vo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.common.utils.Query;

import cn.parzulpan.shopping.product.dao.CategoryDao;
import cn.parzulpan.shopping.product.entity.CategoryEntity;
import cn.parzulpan.shopping.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);

        // 组装成父子的树形结构
        // 找到所有的一级分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid() == 0;
        }).map((menu) -> {
            menu.setChildren(getChildrens(menu, entities));
            return menu;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) -
                    (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 检查当前被删除的菜单，是否被别的地方引用

        // 逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    // [2, 25, 225]
    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return (Long[]) parentPath.toArray(new Long[parentPath.size()]);
    }

    /**
     * 级联更新所有关联的数据
     * 使用 缓存数据一致性的失效模式
     *     @CacheEvict(value = {"category"},key = "'getLevel1Categorys'")
     *     @Caching(evict = {
     *             @CacheEvict(value = {"category"},key = "'getLevel1Categorys'"),
     *             @CacheEvict(value = {"category"},key = "'getCatalogJson'")
     *     })
     */
    @CacheEvict(value = {"category"}, allEntries = true)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Cacheable(value = {"category"}, key = "#root.method.name")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return categoryEntities;
    }

    /**
     * SpringCache 能解决读模式下的缓存穿透和缓存雪崩问题，但是对于缓存击穿没有很好的解决方法。
     * 缓存击穿的解决方法之一就是加锁，但是 SpringCache 的加锁只有在读模式下有本地锁，
     * 所以这个得分场景来确定，对于常规数据 SpringCache 完全够用了，对于一致性要求高的数据还是得使用分布式锁
     */
    @Cacheable(value = {"category"}, key = "#root.methodName", sync = true)
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        // Redisson 可重入锁（Reentrant Lock）
        RLock lock = redissonClient.getLock("getCatalogJson-lock");
        lock.lock(10, TimeUnit.SECONDS);
        Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDbWithoutCache();
        lock.unlock();
        return dataFromDb;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonOld() {
        // 使用 redis
        /**
         * 解决缓存失效：
         * 1. 空结果缓存，并加入短暂过期时间：解决缓存穿透
         * 2. 设置过期时间，并且为随机值：解决缓存雪崩
         * 3. 加锁：解决缓存击穿
         */
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (StringUtils.isEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> catalogJsonFromDb = getCatalogJsonFromDbWithRedisson();
            // 结果放入缓存也在锁中
            return catalogJsonFromDb;
        }

        return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
    }

    /**
     * Redisson 可重入锁（Reentrant Lock）
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisson() {
        RLock lock = redissonClient.getLock("getCatalogJsonFromDbWithRedisson-lock");
        lock.lock(10, TimeUnit.SECONDS);
        Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDb();
        lock.unlock();
        return dataFromDb;
    }

    /**
     * 从数据库获取数据，使用 redis 的分布式锁 V1
     * 问题：
     *  1、setnx 占好了位，业务代码异常或者程序在页面过程中宕机。没有执行删除锁逻辑，这就造成了死锁
     * 解决：
     *  设置锁的自动过期，即使没有删除，会自动删除
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLockV1() {
        // Redis 命令：set lock 1 NX
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "1");
        if ( lock ) {
            // 加锁成功，执行业务
            Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDb();
            // 删除锁
            stringRedisTemplate.delete("lock");
            return dataFromDb;
        } else {
            // 加锁失败，重试
            // 休眠 100ms 重试
            // 自旋的方式
            return getCatalogJsonFromDbWithRedisLockV1();
        }
    }

    /**
     * 从数据库获取数据，使用 redis 的分布式锁 V2
     * 问题：
     *  1、setnx 设置好，正要去设置过期时间，结果突然断电，服务宕机。又死锁了。
     * 解决：
     *  设置过期时间和占位必须是原子的。redis支持使用 setnx ex 命令。
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLockV2() {
        // Redis 命令：set lock 1 NX
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "1");
        if ( lock ) {
            // 加锁成功，执行业务
            // 设置过期时间
            stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDb();
            // 删除锁
            stringRedisTemplate.delete("lock");
            return dataFromDb;
        } else {
            // 加锁失败，重试
            // 休眠 100ms 重试
            // 自旋的方式
            return getCatalogJsonFromDbWithRedisLockV2();
        }
    }

    /**
     * 从数据库获取数据，使用 redis 的分布式锁 V3
     * 问题：
     *  1、如果由于业务时间很长，锁自己过期了，我们直接删除，有可能把别人正在持有的锁删除了。
     * 解决：
     *  占锁的时候，值指定为 uuid，每个人匹配是自己的锁才删除。
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLockV3() {
        // Redis 命令：set lock 1 EX 30 NX
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", "1", 30, TimeUnit.SECONDS);
        if ( lock ) {
            // 加锁成功，执行业务
            Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDb();
            // 删除锁
            stringRedisTemplate.delete("lock");
            return dataFromDb;
        } else {
            // 加锁失败，重试
            // 休眠 100ms 重试
            // 自旋的方式
            return getCatalogJsonFromDbWithRedisLockV3();
        }
    }

    /**
     * 从数据库获取数据，使用 redis 的分布式锁 V4
     * 问题：
     *  1、如果正好判断是当前值，正要删除锁的时候，锁已经过期，别人已经设置到了新的值。那么我们删除的是别人的锁。
     * 解决：
     *  删除锁必须是原子性的。使用 redis+Lua脚本完成。
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLockV4() {
        // Redis 命令：set lock uuid EX 30 NX
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if ( lock ) {
            // 加锁成功，执行业务
            Map<String, List<Catelog2Vo>> dataFromDb = getDataFromDb();
            // 删除锁前先进行获取，判断是不是自己的锁编号 uuid，是的话再删除
            String lockValue = stringRedisTemplate.opsForValue().get("lock");
            if (uuid.equals(lockValue)) {
                stringRedisTemplate.delete("lock");
            }
            return dataFromDb;
        } else {
            // 加锁失败，重试
            // 休眠 100ms 重试
            // 自旋的方式
            return getCatalogJsonFromDbWithRedisLockV4();
        }
    }

    /**
     * 从数据库获取数据，使用 redis 的分布式锁 V5
     * 问题：
     *  1、锁的自动续期问题；
     *  2、操作太麻烦，加锁解锁都需要自己完成，如果有很多锁则需要写很多重复的代码。
     * 解决：
     *  使用封装好的 redis 分布式锁工具类，例如 Redisson
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithRedisLockV5() {
        // Redis 命令：set lock uuid EX 30 NX
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 30, TimeUnit.SECONDS);
        if ( lock ) {
            log.debug("获取分布式锁成功....");
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                //加锁成功，执行业务
                dataFromDb = getDataFromDb();
            } finally {
                //删除锁前先进行获取，判断是不是自己的锁编号uuid，是的话再删除
                //获取对比值+对比成功删除==原子操作  使用lua脚本解锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁，删除成功返回 1，删除失败返回 0
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                        Arrays.asList("lock"), uuid);
            }
            return dataFromDb;
        } else {
            log.debug("获取分布式锁失败，等待重试....");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 加锁失败，重试
            // 休眠 100ms 重试
            // 自旋的方式
            return getCatalogJsonFromDbWithRedisLockV5();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        // 得到锁以后，查询缓存，确认缓存数据
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJSON");
        if (!StringUtils.isEmpty(catalogJSON)) {
            // 缓存数据不为空，直接返回
            return JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
        }
        System.out.println("查询了数据库...");

        // 将数据库的多次查询变为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1. 查出所有 1 级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);

        // 2. 封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2.1 查出每个 1 级分类的 2 级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            // 2.2 封装数据
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(item -> {
                    // 2.2.1 查出每个 2 级分类的 3 级分类
                    List<CategoryEntity> categoryEntities1 = getParentCid(selectList, item.getCatId());
                    // 2.2.2 封装数据
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                    if (categoryEntities1 != null) {
                        catelog3Vos = categoryEntities1.stream().map(item1 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), item1.getCatId().toString(), item1.getName());

                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), catelog3Vos, item.getCatId().toString(), item.getName());

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        // 结果放入缓存也在锁中
        String s = JSON.toJSONString(parentCid);
        stringRedisTemplate.opsForValue().set("catalogJSON", s);

        return parentCid;
    }
    private Map<String, List<Catelog2Vo>> getDataFromDbWithoutCache() {
        // 将数据库的多次查询变为一次
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1. 查出所有 1 级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);

        // 2. 封装数据
        Map<String, List<Catelog2Vo>> parentCid = level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            // 2.1 查出每个 1 级分类的 2 级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            // 2.2 封装数据
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(item -> {
                    // 2.2.1 查出每个 2 级分类的 3 级分类
                    List<CategoryEntity> categoryEntities1 = getParentCid(selectList, item.getCatId());
                    // 2.2.2 封装数据
                    List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                    if (categoryEntities1 != null) {
                        catelog3Vos = categoryEntities1.stream().map(item1 -> {
                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(item.getCatId().toString(), item1.getCatId().toString(), item1.getName());

                            return catelog3Vo;
                        }).collect(Collectors.toList());
                    }
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), catelog3Vos, item.getCatId().toString(), item.getName());

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }

            return catelog2Vos;
        }));

        return parentCid;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLocalLock() {
        // 演示本地锁：synchronized，JUC（ReentrantLock）等，它存在的问题是只能锁住当前进程，不适用分布式场景
        synchronized (this) {
            // 得到锁以后，查询缓存，确认缓存数据
            return getDataFromDb();
        }
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid().equals(parentCid)).collect(Collectors.toList());
        return collect;
//        return baseMapper.selectList(new QueryWrapper<CategoryEntity>()
//                .eq("parent_cid", v.getCatId()));
    }

    // [225, 25, 2]
    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    /**
     * 递归查找所有菜单的子菜单
     */
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> entities = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            // 找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted((menu1, menu2) -> {
            // 菜单排序
            return (menu1.getSort() == null ? 0 : menu1.getSort()) -
                    (menu2.getSort() == null ? 0 : menu2.getSort());
        }).collect(Collectors.toList());

        return entities;
    }

}