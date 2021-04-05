package cn.parzulpan.shopping.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.shopping.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 17:38:46
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> collect);
}

