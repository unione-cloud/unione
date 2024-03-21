package com.unione.cloud.portal.system.model;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	SysOrgan Entity
 * @描述	系统管理：机构信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-21 23:12:15
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysOrgan")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_organ")
public class SysOrgan extends Pojo {
	/**
	* 上级ID
	*/
	@ApiModelProperty(value="上级ID",notes="长度为：19")
	private Long parentId;
	/**
	* 区域label，eg：广州市->天河区
	*/
	@ApiModelProperty(value="区域label，eg：广州市->天河区",notes="长度为：200")
	private String areaLabel;
	/**
	* 企业/机构名称
	*/
	@ApiModelProperty(value="企业/机构名称",notes="长度为：250")
	private String name;
	/**
	* 企业/机构别名
	*/
	@ApiModelProperty(value="企业/机构别名",notes="长度为：250")
	private String alias;
	/**
	* 编码
	*/
	@ApiModelProperty(value="编码",notes="长度为：100")
	private String codes;
	/**
	* 类型：字典ORGTYPES 1企业，2机构，3部门
	*/
	@ApiModelProperty(value="类型：字典ORGTYPES 1企业，2机构，3部门",notes="长度为：10")
	private Integer types;
	/**
	* 主营业务
	*/
	@ApiModelProperty(value="主营业务",notes="长度为：500")
	private String busiMain;
	/**
	* 经营范围
	*/
	@ApiModelProperty(value="经营范围",notes="长度为：500")
	private String busiScop;
	/**
	* 企业/机构地址
	*/
	@ApiModelProperty(value="企业/机构地址",notes="长度为：250")
	private String addr;
	/**
	* 联系电话
	*/
	@ApiModelProperty(value="联系电话",notes="长度为：50")
	private String tel;
	/**
	* 级别
	*/
	@ApiModelProperty(value="级别",notes="长度为：10")
	private Integer levels;
	/**
	* 是否叶子节点 1：是叶子节点 0：非叶子节点
	*/
	@ApiModelProperty(value="是否叶子节点 1：是叶子节点 0：非叶子节点",notes="长度为：10")
	private Integer isLeaf;
	/**
	* 显示顺序
	*/
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	/**
	* 状态
	*/
	@ApiModelProperty(value="状态",notes="长度为：10")
	private Integer status;
	/**
	* 说明
	*/
	@ApiModelProperty(value="说明",notes="长度为：500")
	private String descs;

}
