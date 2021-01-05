package cn.parzulpan.shopping.member.dao;

import cn.parzulpan.shopping.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 20:43:07
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
