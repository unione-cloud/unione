package com.unione.cloud.portal.system.model;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.hibernate.validator.constraints.NotBlank;

import com.unione.cloud.beetsql.annotation.UniQueryKeyWord;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	BaseDict Entity
 * @描述	基础：字典
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-14 14:06:12
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.BaseDict")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="base_dict")
public class BaseDict extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2087834552742633262L;
	/**
	* 上级ID
	*/
	@Schema(title="上级ID",description="长度为：19")
	private Long parentId;
	/**
	* 应用ID
	*/
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 应用名称
	*/
	@Schema(title="应用名称",description="长度为：50")
	private String appName;
	/**
	* 字典名称
	*/
	@UniQueryKeyWord
	@Schema(title="字典名称",description="长度为：100")
	@NotEmpty(message = "字典名称不能为空",groups= {Validator.save.class,Validator.update.class})
	@NotBlank(message = "字典名称不能为空",groups= {Validator.save.class,Validator.update.class})
	private String dictName;
	/**
	* 字典类型，0平台，1租户，2机构
	*/
	@Schema(title="字典类型，0平台，1租户，2机构",description="长度为：10")
	@NotNull(message = "字典类型不能为空",groups= {Validator.save.class,Validator.update.class})
	private Integer dictType;
	/**
	* 字典键
	*/
	@Schema(title="字典键",description="长度为：100")
	@NotEmpty(message = "字典键不能为空",groups= {Validator.save.class,Validator.update.class})
	@NotBlank(message = "字典键不能为空",groups= {Validator.save.class,Validator.update.class})
	private String dictKey;
	/**
	* 字典值
	*/
	@UniQueryKeyWord
	@Schema(title="字典值",description="长度为：1000")
	@NotEmpty(message = "字典值不能为空",groups= {Validator.save.class,Validator.update.class})
	@NotBlank(message = "字典值不能为空",groups= {Validator.save.class,Validator.update.class})
	private String dictValue;
	/**
	* 字典显示设置，json存储{}
	*/
	@Schema(title="字典显示设置，json存储{}",description="长度为：1000")
	private String dictShow;
	/**
	* 排序
	*/
	@Schema(title="排序",description="长度为：5")
	private Integer ordered;
	/**
	* 是否叶子节点，字典TUREORFALSE 1是，0否
	*/
	@Schema(title="是否叶子节点，字典TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isLeaf;
	/**
	* 状态 是否使用，字典USEORNOT 1 使用，0停用
	*/
	@Schema(title="状态 是否使用，字典USEORNOT 1 使用，0停用",description="长度为：10")
	private Integer status;

}
