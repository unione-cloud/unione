package com.unione.cloud.ums.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	UmsMessage Entity
 * @描述	统一消息：消息
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:46
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("ums.UmsMessage")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_message")
public class UmsMessage extends Pojo {
	/**
	* 模板表ID
	*/
	@Schema(title="模板表ID",description="长度为：19")
	private Long tmplId;
	/**
	* 分类ID
	*/
	@Schema(title="分类ID",description="长度为：19")
	private Long categoryId;
	/**
	* 类别 notice：通知，msg：消息
	*/
	@Schema(title="类别 notice：通知，msg：消息",description="长度为：10")
	private String types;
	/**
	* 标题
	*/
	@Schema(title="标题",description="长度为：250")
	private String title;
	/**
	* 内容
	*/
	@Schema(title="内容",description="长度为：65535")
	private String content;
	/**
	* 来源名称，字典UMSMSGFROM 
	*/
	@Schema(title="来源名称，字典UMSMSGFROM ",description="长度为：50")
	private String fromId;
	/**
	* 需要用户进行手动确认  0:不需要 1:需要
	*/
	@Schema(title="需要用户进行手动确认  0:不需要 1:需要",description="长度为：10")
	private Integer isConfirm;
	/**
	* 确认类型 1:只有确认按钮 2:拒绝/接受
	*/
	@Schema(title="确认类型 1:只有确认按钮 2:拒绝/接受",description="长度为：10")
	private Integer confirmType;
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
	* 优先级，字典UMSMSGPRIORITY 1：一级，2：二级，3：三级，4：四级
	*/
	@Schema(title="优先级，字典UMSMSGPRIORITY 1：一级，2：二级，3：三级，4：四级",description="长度为：10")
	private Integer priority;
	/**
	* 发布时间
	*/
	@Schema(title="发布时间",description="长度为：19")
	private Date publicDate;
	/**
	* 下线时间
	*/
	@Schema(title="下线时间",description="长度为：19")
	private Date offlineDate;

}
