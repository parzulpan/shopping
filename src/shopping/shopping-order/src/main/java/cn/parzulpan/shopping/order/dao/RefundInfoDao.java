package cn.parzulpan.shopping.order.dao;

import cn.parzulpan.shopping.order.entity.RefundInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款信息
 * 
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 20:40:58
 */
@Mapper
public interface RefundInfoDao extends BaseMapper<RefundInfoEntity> {
	
}
