package com.unione.cloud.ums.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

/**
 * @标题 	UmsTodo Entity
 * @描述	统一消息：用户待办表
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:46
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("ums.UmsTodo")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_todo")
public class UmsTodo extends Pojo {
	/**
	* 分类ID
	*/
	@Schema(title="分类ID",description="长度为：19")
	@NotNull(message = "分类不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long categoryId;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：50")
	@NotNull(message = "标题不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotBlank(message = "标题不能为空",groups = {Validator.save.class,Validator.update.class})
	private String title;
	/**
	* 待办状态，字典UMSTODOSTS 1待处理，2处理中，3已超时，4已完成
	*/
	@Schema(title="待办状态，字典UMSTODOSTS 1待处理，2处理中，3已超时，4已完成",description="长度为：10")
	private Integer status;
	/**
	* 来源名称，字典UMSTODOFROM workflow：工作流
	*/
	@Schema(title="来源名称，字典UMSTODOFROM workflow：工作流",description="长度为：50")
	private String fromId;
	/**
	* 业务ID
	*/
	@Schema(title="业务ID",description="长度为：100")
	private String bizId;
	/**
	* 业务参数,eg:name=zs&sex=1
	*/
	@Schema(title="业务参数,eg:name=zs&sex=1",description="长度为：500")
	private String bizParam;
	/**
	* 查阅状态，字典TUREORFALSE 1是，0否
	*/
	@Schema(title="查阅状态，字典TUREORFALSE 1是，0否",description="长度为：10")
	private Integer viewSts;
	/**
	* 查阅时间
	*/
	@Schema(title="查阅时间",description="长度为：19")
	private Date viewTime;
	/**
	* 优先级，字典UMSTODOPRIORITY 1：一级，2：二级，3：三级，4：四级
	*/
	@Schema(title="优先级，字典UMSTODOPRIORITY 1：一级，2：二级，3：三级，4：四级",description="长度为：10")
	private Integer priority;

}
