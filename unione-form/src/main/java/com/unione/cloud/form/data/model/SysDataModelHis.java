package com.unione.cloud.form.data.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	SysDataModelHis Entity
 * @描述	系统管理：数据模型历史
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataModelHis")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_model_his")
public class SysDataModelHis extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4172393954843642188L;
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
	* 数据模型ID
	*/
	@ApiModelProperty(value="数据模型ID",notes="长度为：19")
	private Long modelId;
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
	* 版本号
	*/
	@ApiModelProperty(value="版本号",notes="长度为：10")
	private Integer vers;
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
	* 表单模版，字典DMPSFORMTMPL normal：常规
	*/
	@ApiModelProperty(value="表单模版，字典DMPSFORMTMPL normal：常规",notes="长度为：50")
	private String formTmpl;
	/**
	* 表单类型，字典DMSFORMTYPE 1：单表，2：主表，3附表
	*/
	@ApiModelProperty(value="表单类型，字典DMSFORMTYPE 1：单表，2：主表，3附表",notes="长度为：10")
	private Integer formType;
	/**
	* 显示方式，字典DMSDATASHOWTTYPE table：表格，tabletree：表格树
	*/
	@ApiModelProperty(value="显示方式，字典DMSDATASHOWTTYPE table：表格，tabletree：表格树",notes="长度为：20")
	private String showType;
	/**
	* 数据配置，json存储,{}
            {
                list:{列表页面配置},
                form:{表单页面配置},
                view:{浏览页面配置},
                api:{接口配置}
            }
	*/
	@ApiModelProperty(value="数据配置，json存储,{}"+
            "{"+
            "    list:{列表页面配置},"+
            "    form:{表单页面配置},"+
            "    view:{浏览页面配置},"+
            "    api:{接口配置}"+
            "}",notes="长度为：2147483647")
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
	* 是否演练模式，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="是否演练模式，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer skitsFlag;
	/**
	* 说明
	*/
	@ApiModelProperty(value="说明",notes="长度为：500")
	private String descs;

}
