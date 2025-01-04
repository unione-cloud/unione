package com.unione.cloud.portal.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	BaseDictSetting Entity
 * @描述	基础：字典设置，租户，机构字典设置
 * @作者	Unione Cloud CodeGen
 * @日期	2024-12-14 14:06:12
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.BaseDictSetting")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="base_dict_setting")
public class BaseDictSetting extends Pojo {
	/**
	* 上级ID
	*/
	@ApiModelProperty(value="上级ID",notes="长度为：19")
	private Long parentId;
	/**
	* 字典ID
	*/
	@ApiModelProperty(value="字典ID",notes="长度为：19")
	private Long dictId;
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	private Long appId;
	/**
	* 应用名称
	*/
	@ApiModelProperty(value="应用名称",notes="长度为：50")
	private String appName;
	/**
	* 字典名称
	*/
	@ApiModelProperty(value="字典名称",notes="长度为：100")
	private String dictName;
	/**
	* 字典类型，0平台，1租户，2机构
	*/
	@ApiModelProperty(value="字典类型，0平台，1租户，2机构",notes="长度为：10")
	private Integer dictType;
	/**
	* 字典键
	*/
	@ApiModelProperty(value="字典键",notes="长度为：100")
	private String dictKey;
	/**
	* 字典值
	*/
	@ApiModelProperty(value="字典值",notes="长度为：100")
	private String dictValue;
	/**
	* 字典显示设置，json存储{}
	*/
	@ApiModelProperty(value="字典显示设置，json存储{}",notes="长度为：1000")
	private String dictShow;
	/**
	* 排序
	*/
	@ApiModelProperty(value="排序",notes="长度为：10")
	private Integer ordered;
	/**
	* 是否叶子节点，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="是否叶子节点，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer isLeaf;
	/**
	* 状态 是否使用，字典USEORNOT 1 使用，0停用
	*/
	@ApiModelProperty(value="状态 是否使用，字典USEORNOT 1 使用，0停用",notes="长度为：10")
	private Integer status;

}
