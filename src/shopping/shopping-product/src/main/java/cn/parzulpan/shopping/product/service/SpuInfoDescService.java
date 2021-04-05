package cn.parzulpan.shopping.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.shopping.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 17:38:46
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDescSpuInfo(SpuInfoDescEntity spuInfoDescEntity);
}

