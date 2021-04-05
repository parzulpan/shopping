package cn.parzulpan.shopping.product.service;

import cn.parzulpan.shopping.product.entity.BrandEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.shopping.product.entity.CategoryBrandRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 分类&品牌关联
 *
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-04-04 16:41:49
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long brandId, String name);

    void updateCategory(Long catId, String name);

    List<BrandEntity> getBrandsByCatId(Long catId);
}

