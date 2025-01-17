package com.unione.cloud.core.model;

import java.io.Serializable;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
	@Schema(title="主键ID")
	@NotNull(message = "主键为空",groups = {Validator.update.class})
	private Long id;
	
	/**
	 * 租户ID
	 */
	@Schema(title="租户ID")
	private Long tenantId;
	
	/**
	 * 机构ID
	 */
	@Schema(title="机构ID")
	private Long orgId;
	
	/**
	 * 用户ID
	 */
	@Schema(title="用户ID")
	private Long userId;
	/**
	 * 删除标记，0:正常,1:已删除
	 */
	@Schema(title="删除标记，0:正常,1:已删除",description="字符长度为：10")
	private Integer delFlag;
	
	/**
	 * 创建时间
	 */
	@Schema(title="创建时间",description = "时间格式:yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date created;
	
	/**
	 * 创建人（账号）
	 */
	@Schema(title="创建人")
	private Long createdBy;
	
	/**
	 * 修改时间
	 */
	@Schema(title="修改时间",description = "时间格式:yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date lastUpdated;
	
	/**
	 * 修改人（账号）
	 */
	@Schema(title="修改人")
	private Long lastUpdatedBy;
	
}
