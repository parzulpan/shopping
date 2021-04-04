package cn.parzulpan.shopping.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.shopping.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 17:38:46
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> asList);

    /**
     * 找到 catelogId 的完整路径
     * 格式为 [父/子/孙]
     * @param catelogId
     * @return
     */
    Long[] findCatelogPath(Long catelogId);
}

