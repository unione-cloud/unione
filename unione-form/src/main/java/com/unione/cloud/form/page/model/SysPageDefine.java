package com.unione.cloud.form.page.model;
import java.util.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

/**
 * @标题 	SysPageDefine Entity
 * @描述	系统管理：页面定义
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:44:00
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("page.SysPageDefine")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_page_define")
public class SysPageDefine extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3564319336714244891L;
	@AssignID
	private Long sid;
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 模版ID
	*/
	@ApiModelProperty(value="模版ID",notes="长度为：19")
	private Long tmplId;
	/**
	* 页面组件
	*/
	@ApiModelProperty(value="页面组件",notes="长度为：100")
	@NotNull(message="页面组件不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotEmpty(message="页面组件不能为空",groups = {Validator.save.class,Validator.update.class})
	private String component;
	/**
	* 页面标题
	*/
	@ApiModelProperty(value="页面标题",notes="长度为：100")
	@NotNull(message="页面标题不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotEmpty(message="页面标题不能为空",groups = {Validator.save.class,Validator.update.class})
	private String title;
	/**
	* 页面编码，唯一
	*/
	@ApiModelProperty(value="页面编码，唯一",notes="长度为：50")
	private String sn;
	/**
	* 版本号
	*/
	@ApiModelProperty(value="版本号",notes="长度为：10")
	private Integer vers;
	/**
	* 概要信息
	*/
	@ApiModelProperty(value="概要信息",notes="长度为：250")
	private String summary;
	/**
	* 图标（字体图标）
	*/
	@ApiModelProperty(value="图标（字体图标）",notes="长度为：100")
	private String icon;
	/**
	* 大图标(图片图标)
	*/
	@ApiModelProperty(value="大图标(图片图标)",notes="长度为：250")
	private String picMax;
	/**
	* 中图标(图片图标)
	*/
	@ApiModelProperty(value="中图标(图片图标)",notes="长度为：250")
	private String picMid;
	/**
	* 小图标(图片图标)
	*/
	@ApiModelProperty(value="小图标(图片图标)",notes="长度为：250")
	private String picMix;
	/**
	* 页面分类，字典PAGETYPE  code:编码型页面，setting：配置型页面，design：设计型页面
	*/
	@ApiModelProperty(value="页面分类，字典PAGETYPE  code:编码型页面，setting：配置型页面，design：设计型页面",notes="长度为：20")
	@NotNull(message="页面分类不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotEmpty(message="页面分类不能为空",groups = {Validator.save.class,Validator.update.class})
	private String types;
	/**
	* 行业分类，多个逗号分隔
	*/
	@ApiModelProperty(value="行业分类，多个逗号分隔",notes="长度为：500")
	private String trades;
	/**
	* 预览图片url
	*/
	@ApiModelProperty(value="预览图片url",notes="长度为：250")
	private String reviewPic;
	/**
	* 页面定义，json结构
            ｛
            
            
            ｝
	*/
	@ApiModelProperty(value="页面定义，json结构｛｝",notes="长度为：2147483647")
	@NotNull(message="页面定义不能为空",groups = {Validator.save.class,Validator.update.class})
	@NotEmpty(message="页面定义不能为空",groups = {Validator.save.class,Validator.update.class})
	private String configs;
	/**
	* 是否模版
	*/
	@ApiModelProperty(value="是否模版",notes="长度为：10")
	private Integer isTmpl;
	/**
	* 平台页面，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="平台页面，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer isGlobal;
	/**
	* 页面状态，字典PAGESTATUS 1新建，2发布，3修改，4撤回
	*/
	@ApiModelProperty(value="页面状态，字典PAGESTATUS 1新建，2发布，3修改，4撤回",notes="长度为：10")
	private Integer status;
	/**
	* 备注
	*/
	@ApiModelProperty(value="备注",notes="长度为：200")
	private String descs;

}
