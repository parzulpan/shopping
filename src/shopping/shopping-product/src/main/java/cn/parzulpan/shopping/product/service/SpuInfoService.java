package cn.parzulpan.shopping.product.service;

import cn.parzulpan.shopping.product.vo.SpuSaveVo;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.shopping.product.entity.SpuInfoEntity;

import java.util.Map;

/**
 * spu信息
 *
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 17:38:46
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 商品上架，信息存储在 es 中
     * @param spuId spuId
     */
    void up(Long spuId);
}

