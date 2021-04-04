package cn.parzulpan.shopping.product.dao;

import cn.parzulpan.shopping.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 分类&品牌关联
 * 
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-04-04 16:41:49
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    @Update("update `pms_category_brand_relation` set catelog_name = #{name} where catelog_id = #{catId}")
    void updateCategory(@Param("catId") Long catId, @Param("name") String name);
}
