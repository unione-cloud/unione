package com.unione.cloud.form.data.model;
import java.util.Date;

import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.unione.cloud.beetsql.annotation.UniDataPermis;
import com.unione.cloud.beetsql.annotation.UniQueryIgnore;
import com.unione.cloud.beetsql.annotation.UniQueryIgnore.QueryType;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @标题 	SysDataDefineRelease Entity
 * @描述	系统管理：数据定义发布
 * @作者	Unione Cloud CodeGen
 * @日期	2024-09-05 23:35:09
 * @版本	1.0.0
 **/
@Data
@Builder
@UniDataPermis
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_define_release")
@SqlResource("data.SysDataDefineRelease")
public class SysDataDefineRelease extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4825993495959160118L;
	/**
	* 应用ID
	*/
	@Schema(title="应用ID",description="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 目录ID
	*/
	@Schema(title="目录ID",description="长度为：19")
	private Long dirId;
	/**
	* 数据源ID
	*/
	@Schema(title="数据源ID",description="长度为：19")
	private Long dsId;
	/**
	* 数据标题
	*/
	@Schema(title="数据标题",description="长度为：100")
	private String title;
	/**
	* 数据名称，对应数据库表名称
	*/
	@Schema(title="数据名称，对应数据库表名称",description="长度为：100")
	private String name;
	/**
	* 数据编码
	*/
	@Schema(title="数据编码",description="长度为：50")
	private String sn;
	/**
	* 版本号
	*/
	@Schema(title="版本号",description="长度为：10")
	private Integer vers;
	/**
	* 类别，sql：关系型存储，nosql：非关系型存储，api：接口存储
	*/
	@Schema(title="类别，sql：关系型存储，nosql：非关系型存储，api：接口存储",description="长度为：10")
	private String category;
	/**
	* URL，api接口url，不包含服务ctx部分,/开头，eg：/user
	*/
	@Schema(title="api接入url",description="api接口url，不包含服务ctx部分,/开头，eg：/user,长度为：100")
	private String url;
	/**
	* 自定义，字典TUREORFALSE 1是，0否
	*/
	@Schema(title="自定义，字典TUREORFALSE 1是，0否",description="长度为：10")
	private Integer isCustom;
	/**
	* 发布日期
	*/
	@Schema(title="发布日期",description="长度为：19")
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	private Date publishDate;
	/**
	* 数据查询脚本
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="数据查询脚本",description="长度为：65535")
	private String findScript;
	/**
	* 数据新增脚本
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="数据新增脚本",description="长度为：65535")
	private String insertScript;
	/**
	* 数据更新脚本
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="数据更新脚本",description="长度为：65535")
	private String updateScript;
	/**
	* 数据删除脚本
	*/
	@UniQueryIgnore(QueryType.SELECT_LIST)
	@Schema(title="数据删除脚本",description="长度为：65535")
	private String deleteScript;
	/**
	* 同步状态，是否已同步数据库，字典TUREORFALSE 1是，0否
	*/
	@Schema(title="同步状态，是否已同步数据库，字典TUREORFALSE 1是，0否",description="长度为：10")
	private Integer syncFlag;
	/**
	* 数据配置，json存储,{}
	*/
	@Schema(title="数据配置，json存储,{}",description="长度为：65535")
	private String configs;
	/**
	* 数据签名
	*/
	@Schema(title="数据签名",description="数据签名，（configs，xxxxScript等）字段hash运算后的值,长度为：100")
	private String signature;
	/**
	* 显示顺序
	*/
	@Schema(title="显示顺序",description="长度为：10")
	private Integer ordered;
	/**
	* 发布状态，字典PUBLISHSTATUS 0新建，1提交，2发布，3撤回
	*/
	@Schema(title="发布状态，字典PUBLISHSTATUS 0新建，1提交，2发布，3撤回",description="长度为：10")
	private Integer status;
	/**
	* 说明
	*/
	@Schema(title="说明",description="长度为：500")
	private String descs;

}
