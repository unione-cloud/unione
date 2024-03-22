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
 * @标题 	SysApiInfo Entity
 * @描述	系统管理：接口信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-22 08:03:37
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysApiInfo")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_api_info")
public class SysApiInfo extends Pojo {
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	private Long appId;
	/**
	* 上级ID(根节点为-1)
	*/
	@ApiModelProperty(value="上级ID(根节点为-1)",notes="长度为：19")
	private Long parentId;
	/**
	* 名称（英文名称，一个应用中保持唯一）
	*/
	@ApiModelProperty(value="名称（英文名称，一个应用中保持唯一）",notes="长度为：50")
	private String name;
	/**
	* 标题
	*/
	@ApiModelProperty(value="标题",notes="长度为：100")
	private String title;
	/**
	* URL地址,/开头，不包含应用ctx
	*/
	@ApiModelProperty(value="URL地址,/开头，不包含应用ctx",notes="长度为：250")
	private String url;
	/**
	* 请求方式，多个用逗号 '','' 分隔。字典 REQUESTMETHOD:POST,GET,PUT,DELETE,PATCH,OPTIONS,HEAD
	*/
	@ApiModelProperty(value="请求方式，多个用逗号 '','' 分隔。字典 REQUESTMETHOD:POST,GET,PUT,DELETE,PATCH,OPTIONS,HEAD",notes="长度为：50")
	private String method;
	/**
	* 请求参数
	*/
	@ApiModelProperty(value="请求参数",notes="长度为：2147483647")
	private String params;
	/**
	* 接口响应
	*/
	@ApiModelProperty(value="接口响应",notes="长度为：2147483647")
	private String response;
	/**
	* 是否叶子节点，字典 TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="是否叶子节点，字典 TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer isLeaf;
	/**
	* 是否需要授权，字典 TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="是否需要授权，字典 TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer isNeedPermis;
	/**
	* 显示顺序
	*/
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	/**
	* 状态，字典，使用状态 USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="状态，字典，使用状态 USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;
	/**
	* 描述
	*/
	@ApiModelProperty(value="描述",notes="长度为：500")
	private String descs;
	/**
	* 文档内容
	*/
	@ApiModelProperty(value="文档内容",notes="长度为：2147483647")
	private String docBody;

}
