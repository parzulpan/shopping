package cn.parzulpan.shopping.ware.dao;

import cn.parzulpan.shopping.ware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 20:42:08
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
