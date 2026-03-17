package com.unione.cloud.system.model;
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
 * @标题 	SysUserBind Entity
 * @描述	系统管理：用户绑定，第三方平台帐号绑定
 * @作者	Unione Cloud CodeGen
 * @日期	2026-03-17 11:44:11
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("system.SysUserBind")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_user_bind")
public class SysUserBind extends Pojo {
	/**
	* 平台标识，eg：weixin微信，qq：QQ等等
	*/
	@Schema(title="平台标识，eg：weixin微信，qq：QQ等等",description="长度为：20")
	private String platKey;
	/**
	* 用户数据,json存储
	*/
	@Schema(title="用户数据,json存储",description="长度为：65535")
	private String platData;
	/**
	* 用户标识
	*/
	@Schema(title="用户标识",description="长度为：100")
	private String openId;
	/**
	* 联合标识
	*/
	@Schema(title="联合标识",description="长度为：100")
	private String unionId;

}
