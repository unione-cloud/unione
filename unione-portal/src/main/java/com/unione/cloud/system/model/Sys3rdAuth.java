package com.unione.cloud.system.model;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.QueryIgnore;
import com.unione.cloud.beetsql.annotation.QueryIgnore.QueryType;
import com.unione.cloud.beetsql.builder.SqlAction;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	Sys3rdAuth Entity
 * @描述	系统管理：第三方系统认证，所有请求外部系统认证管理，前端接口动态加载认证信息（后端加密，前端解密）
 * @作者	Unione Cloud CodeGen
 * @日期	2026-07-03 09:39:18
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis
@SqlResource("system.Sys3rdAuth")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_3rd_auth")
public class Sys3rdAuth extends Pojo {
	/**
	* 场景，字典3RDSCENE ai-llm：AI大模型，asr-rt：实时语音识别， other：其他
	*/
	@NotNull(message="场景不能为空",groups={Validator.save.class})
	@NotEmpty(message="场景不能为空",groups={Validator.save.class})
	@Schema(title="场景，字典3RDSCENE ai-llm：AI大模型，asr-rt：实时语音识别， other：其他",description="长度为：50")
	private String scene;
	/**
	* 标题
	*/
	@NotNull(message="标题不能为空",groups={Validator.save.class})
	@NotEmpty(message="标题不能为空",groups={Validator.save.class})
	@Schema(title="标题",description="长度为：200")
	private String title;
	/**
	* 编码，唯一验证
	*/
	@NotNull(message="编码不能为空",groups={Validator.save.class})
	@NotEmpty(message="编码不能为空",groups={Validator.save.class})
	@Schema(title="编码，唯一验证",description="长度为：50")
	private String sn;
	/**
	* 图标（字体图标）
	*/
	@Schema(title="图标（字体图标）",description="长度为：100")
	private String icon;
	/**
	* 大图标(图片图标)
	*/
	@Schema(title="大图标(图片图标)",description="长度为：250")
	private String picMax;
	/**
	* 中图标(图片图标)
	*/
	@Schema(title="中图标(图片图标)",description="长度为：250")
	private String picMid;
	/**
	* 小图标(图片图标)
	*/
	@Schema(title="小图标(图片图标)",description="长度为：250")
	private String picMix;
	/**
	* 认证接口
	*/
	@NotNull(message="认证接口不能为空",groups={Validator.save.class})
	@NotEmpty(message="认证接口不能为空",groups={Validator.save.class})
	@Schema(title="认证接口",description="长度为：250")
	private String url;
	/**
	* 认证信息，json存储
	*/
	@QueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="认证信息，json存储",description="")
	private String dataJson;
	/**
	* 认证脚本
	*/
	@QueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="认证脚本",description="长度为：65535")
	private String scriptTxt;
	/**
	* 技术文档url
	*/
	@Schema(title="技术文档url",description="长度为：200")
	private String docLink;
	/**
	* 说明
	*/
	@Schema(title="说明",description="长度为：500")
	private String descs;
	/**
	* 全局认证，字典TRUEORFALSE 1是，0否
	*/
	@Schema(title="全局认证，字典TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer isGlobal;
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
