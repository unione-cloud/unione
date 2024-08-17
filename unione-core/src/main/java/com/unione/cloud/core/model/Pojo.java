package com.unione.cloud.core.model;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @描述 <p>系统持久化对象基类，通过泛型类设计进行扩展，所有持久层的类都应该继承自Pojo类.
 * @author Jeking Yang
 * @since 1.0.0
 */
@Data
public class Pojo implements Serializable {

	private static final long serialVersionUID = -4658799939880991528L;
	
	/**
	 * 主键ID
	 */
	@ApiModelProperty("主键ID")
	@NotNull(message = "主键为空",groups = {Validator.update.class})
	private Long id;
	
	/**
	 * 租户ID
	 */
	@ApiModelProperty("租户ID")
	private Long tenantId;
	
	/**
	 * 机构ID
	 */
	@ApiModelProperty("机构ID")
	private Long orgId;
	
	/**
	 * 用户ID
	 */
	@ApiModelProperty("用户ID")
	private Long userId;
	/**
	 * 删除标记，0:正常,1:已删除
	 */
	@ApiModelProperty(value="删除标记，0:正常,1:已删除",notes="字符长度为：10")
	private Integer delFlag;
	
	/**
	 * 创建时间
	 */
	@ApiModelProperty(value="创建时间",notes = "时间格式:yyyy-MM-dd HH:mm:ss")
	private Long created;
	
	/**
	 * 创建人（账号）
	 */
	@ApiModelProperty("创建人")
	private String createdBy;
	
	/**
	 * 修改时间
	 */
	@ApiModelProperty(value="修改时间",notes = "时间格式:yyyy-MM-dd HH:mm:ss")
	private Long lastUpdated;
	
	/**
	 * 修改人（账号）
	 */
	@ApiModelProperty("修改人")
	private String lastUpdatedBy;
    	
	
	////////非持久化属性
	/**
	 * 主键ID
	 */
	@ApiModelProperty("主键ID集合")
	private List<Long> ids;
	
}
