package cn.parzulpan.shopping.coupon.dao;

import cn.parzulpan.shopping.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 20:44:19
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
