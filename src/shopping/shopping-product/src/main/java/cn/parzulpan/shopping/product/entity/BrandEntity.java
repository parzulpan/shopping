package cn.parzulpan.shopping.product.entity;

import cn.parzulpan.common.valid.AddGroup;
import cn.parzulpan.common.valid.ListValue;
import cn.parzulpan.common.valid.UpdateGroup;
import cn.parzulpan.common.valid.UpdateStatusGroup;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 * 
 * @author parzulpan
 * @email parzulpan@gmail.com
 * @date 2021-01-04 17:38:46
 *
 * @NotNull 注解禁止元素为 null，能够接收任何类型
 * @NotEmpty 注解修饰的字段不能为 null 或 ""
 * @NotBlank 注解不能为 null，并且至少包含一个非空格字符。接收字符序列。
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌 id
	 */
	@NotNull(message = "修改必须指定品牌 id", groups = {UpdateGroup.class})
	@Null(message = "新增不能指定品牌 id", groups = {AddGroup.class})
	@TableId
	private Long brandId;

	/**
	 * 品牌名
	 */
	@NotBlank(message = "品牌名不能为空", groups = {AddGroup.class, UpdateGroup.class})
	private String name;

	/**
	 * 品牌 logo 地址
	 */
	@NotBlank(groups = {AddGroup.class})
	@URL(message = "logo 必须是一个合法的 url 地址", groups = {AddGroup.class, UpdateGroup.class})
	private String logo;

	/**
	 * 介绍
	 */
	private String descript;

	/**
	 * 显示状态[0-不显示；1-显示]
	 */

	@NotNull(groups = {AddGroup.class, UpdateStatusGroup.class})
	@ListValue(values={0, 1}, groups = {AddGroup.class, UpdateStatusGroup.class})
	private Integer showStatus;

	/**
	 * 检索首字母必须是一个英文字母
	 */
	@NotEmpty(groups = {AddGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个英文字母", groups = {AddGroup.class, UpdateGroup.class})
	private String firstLetter;

	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class})
	@Min(value = 0, message = "排序必须是一个大于等于 0 的整数", groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
