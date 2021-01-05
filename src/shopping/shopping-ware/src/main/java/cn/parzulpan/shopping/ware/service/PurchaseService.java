package cn.parzulpan.shopping.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import cn.parzulpan.common.utils.PageUtils;
import cn.parzulpan.shopping.ware.entity.PurchaseEntity;

import java.util.Map;

/**
 * 采购信息
 *
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 20:42:08
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

