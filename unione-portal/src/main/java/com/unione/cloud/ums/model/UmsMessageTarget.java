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
 * @标题 	UmsMessageTarget Entity
 * @描述	统一消息：消息对象
 * @作者	Unione Cloud CodeGen
 * @日期	2025-06-19 08:40:47
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("ums.UmsMessageTarget")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="ums_message_target")
public class UmsMessageTarget extends Pojo {
	/**
	* 消息ID
	*/
	@Schema(title="消息ID",description="长度为：19")
	private Long messageId;
	/**
	* 目标类型，字典UMSMSGTARGET 1：全局，2：租户，3：机构，4：用户，5：角色
	*/
	@Schema(title="目标类型，字典UMSMSGTARGET 1：全局，2：租户，3：机构，4：用户，5：角色",description="长度为：10")
	private Integer targetType;
	/**
	* 目标ID
	*/
	@Schema(title="目标ID",description="长度为：19")
	private Long targetId;

}
