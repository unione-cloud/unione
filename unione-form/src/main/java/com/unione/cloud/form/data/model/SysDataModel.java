package com.unione.cloud.form.data.model;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

/**
 * @标题 	SysDataModel Entity
 * @描述	系统管理：数据模型
 * @作者	Unione Cloud CodeGen
 * @日期	2024-09-05 23:35:09
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataModel")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_model")
public class SysDataModel extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6791318702763495868L;
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 目录ID
	*/
	@ApiModelProperty(value="目录ID",notes="长度为：19")
	private Long dirId;
	/**
	* 数据源ID
	*/
	@ApiModelProperty(value="数据源ID",notes="长度为：19")
	private Long dsId;
	/**
	* 数据标题
	*/
	@ApiModelProperty(value="数据标题",notes="长度为：100")
	private String title;
	/**
	* 数据名称，对应数据库表名称
	*/
	@ApiModelProperty(value="数据名称，对应数据库表名称",notes="长度为：100")
	private String name;
	/**
	* 数据编码
	*/
	@ApiModelProperty(value="数据编码",notes="长度为：50")
	private String sn;
	/**
	* 版本号
	*/
	@ApiModelProperty(value="版本号",notes="长度为：10")
	private Integer vers;
	/**
	* 类别，sql：关系型存储，nosql：非关系型存储，api：接口存储
	*/
	@ApiModelProperty(value="类别，sql：关系型存储，nosql：非关系型存储，api：接口存储",notes="长度为：10")
	private String category;
	/**
	* 自定义，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="自定义，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer isCustom;
	/**
	* 发布日期
	*/
	@ApiModelProperty(value="发布日期",notes="长度为：19")
	private Date publishDate;
	/**
	* 数据查询脚本
	*/
	@ApiModelProperty(value="数据查询脚本",notes="长度为：65535")
	private String sqlFind;
	/**
	* 数据新增脚本
	*/
	@ApiModelProperty(value="数据新增脚本",notes="长度为：65535")
	private String sqlInsert;
	/**
	* 数据更新脚本
	*/
	@ApiModelProperty(value="数据更新脚本",notes="长度为：65535")
	private String sqlUpdate;
	/**
	* 数据删除脚本
	*/
	@ApiModelProperty(value="数据删除脚本",notes="长度为：65535")
	private String sqlDelete;
	/**
	* 同步状态，是否已同步数据库，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="同步状态，是否已同步数据库，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer syncFlag;
	/**
	* 数据配置，json存储,{}
	*/
	@JsonIgnore
	@ApiModelProperty(value="数据配置，json存储,{}",notes="长度为：65535")
	private String configs;
	/**
	* 显示顺序
	*/
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	/**
	* 发布状态，字典PUBLISHSTATUS 0新建，1提交，2发布，3撤回
	*/
	@ApiModelProperty(value="发布状态，字典PUBLISHSTATUS 0新建，1提交，2发布，3撤回",notes="长度为：10")
	private Integer status;
	/**
	* 说明
	*/
	@ApiModelProperty(value="说明",notes="长度为：500")
	private String descs;

}
