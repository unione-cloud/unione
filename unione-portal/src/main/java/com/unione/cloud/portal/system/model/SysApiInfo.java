package com.unione.cloud.portal.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysApiInfo Entity
 * @描述	系统管理：接口信息
 * @作者	Unione Cloud CodeGen
 * @日期	2024-03-25 20:34:17
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
	@Schema(title="应用ID",description="长度为：19")
	private Long appId;
	/**
	* 上级ID(根节点为-1)
	*/
	@Schema(title="上级ID(根节点为-1)",description="长度为：19")
	private Long parentId;
	/**
	* 名称（英文名称，一个应用中保持唯一）
	*/
	@Schema(title="名称（英文名称，一个应用中保持唯一）",description="长度为：50")
	private String name;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：100")
	private String title;
	/**
	* URL地址,/开头，不包含应用ctx
	*/
	@Schema(title="URL地址,/开头，不包含应用ctx",description="长度为：250")
	private String url;
	/**
	* 请求方式，多个用逗号 '','' 分隔。字典 REQUESTMETHOD:POST,GET,PUT,DELETE,PATCH,OPTIONS,HEAD
	*/
	@Schema(title="请求方式，多个用逗号 '','' 分隔。字典 REQUESTMETHOD:POST,GET,PUT,DELETE,PATCH,OPTIONS,HEAD",description="长度为：50")
	private String method;
	/**
	* 请求参数
	*/
	@Schema(title="请求参数",description="长度为：2147483647")
	private String params;
	/**
	* 接口响应
	*/
	@Schema(title="接口响应",description="长度为：2147483647")
	private String response;
	/**
	* 是否叶子节点，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否叶子节点，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isLeaf;
	/**
	* 是否需要授权，字典 TUREORFALSE 1是，0否
	*/
	@Schema(title="是否需要授权，字典 TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isNeedPermis;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 状态，字典，使用状态 USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典，使用状态 USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;
	/**
	* 文档内容
	*/
	@Schema(title="文档内容",description="长度为：2147483647")
	private String docBody;

}
