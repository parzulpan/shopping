package cn.parzulpan.shopping.coupon.dao;

import cn.parzulpan.shopping.coupon.entity.SeckillSkuRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 秒杀活动商品关联
 * 
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 20:44:19
 */
@Mapper
public interface SeckillSkuRelationDao extends BaseMapper<SeckillSkuRelationEntity> {
	
}
