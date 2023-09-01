package com.unione.cloud.core.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

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
	 * 创建时间
	 */
	@ApiModelProperty(value="创建时间",notes = "时间格式:yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date created;
	
	/**
	 * 创建人（账号）
	 */
	@ApiModelProperty("创建人")
	private String createdBy;
	
	/**
	 * 修改时间
	 */
	@ApiModelProperty(value="修改时间",notes = "时间格式:yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date lastUpdated;
	
	/**
	 * 修改人（账号）
	 */
	@ApiModelProperty("修改人ID")
	private String lastUpdatedBy;
    	
}
