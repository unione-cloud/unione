package com.unione.cloud.ums.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	UmsCategory Entity
 * @描述	统一消息：消息分类
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:47
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("ums.UmsCategory")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_category")
public class UmsCategory extends Pojo {
	/**
	* 标题
	*/
	@NotNull(message = "标题不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message = "标题不能为空",groups = {Validator.save.class,Validator.update.class})
	@Schema(title="标题",description="长度为：50")
	private String title;
	/**
	* 消息类别 notice：通知，msg：消息 todo：待办
	*/
	@Schema(title="消息类别 notice：通知，msg：消息 todo：待办",description="长度为：10")
	private String types;
	/**
	* 链接地址
	*/
	@Schema(title="链接地址",description="长度为：250")
	private String url;
	/**
	* 备注
	*/
	@Schema(title="备注",description="长度为：500")
	private String remark;
	/**
	* 使用级别，字典UMSCATEGORYUSEL 1：全局，2：租户，3：机构
	*/
	@Schema(title="使用级别，字典UMSCATEGORYUSEL 1：全局，2：租户，3：机构",description="长度为：10")
	private Integer usel;
	/**
	* 状态，字典USEORNOT 1使用，0停用
	*/
	@Schema(title="状态，字典USEORNOT 1使用，0停用",description="长度为：10")
	private Integer status;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;

}
