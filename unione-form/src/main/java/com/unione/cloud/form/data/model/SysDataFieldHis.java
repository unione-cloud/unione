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

import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.model.Validator;

/**
 * @标题 	SysDataFieldHis Entity
 * @描述	系统管理：数据字段历史
 * @作者	Unione Cloud CodeGen
 * @日期	2024-06-15 07:47:11
 * @版本	1.0.0
 **/
@Data
@Builder
@SqlResource("data.SysDataFieldHis")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="sys_data_field_his")
public class SysDataFieldHis extends Pojo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5559512447536939791L;
	/**
	* 应用ID
	*/
	@ApiModelProperty(value="应用ID",notes="长度为：19")
	@NotNull(message="应用ID不能为空",groups = {Validator.save.class,Validator.update.class})
	private Long appId;
	/**
	* 数据模型历史ID
	*/
	@ApiModelProperty(value="数据模型历史ID",notes="长度为：19")
	private Long modelHisId;
	/**
	* 标题
	*/
	@ApiModelProperty(value="标题",notes="长度为：100")
	private String title;
	/**
	* 名称
	*/
	@ApiModelProperty(value="名称",notes="长度为：50")
	private String name;
	/**
	* 数据类型，直接使用java映射类型，如：String，Double，Float，Boolean，Date 等
	*/
	@ApiModelProperty(value="数据类型，直接使用java映射类型，如：String，Double，Float，Boolean，Date 等",notes="长度为：20")
	private String dataType;
	/**
	* 数据格式
	*/
	@ApiModelProperty(value="数据格式",notes="长度为：50")
	private String dataFormat;
	/**
	* 数据长度
	*/
	@ApiModelProperty(value="数据长度",notes="长度为：10")
	private Integer dataLen;
	/**
	* 数据精度
	*/
	@ApiModelProperty(value="数据精度",notes="长度为：10")
	private Integer dataPrec;
	/**
	* 是否主键，字典TUREORNOT 1是，0否
	*/
	@ApiModelProperty(value="是否主键，字典TUREORNOT 1是，0否",notes="长度为：10")
	private Integer isPk;
	/**
	* 是否外键，字典TUREORNOT 1是，0否
	*/
	@ApiModelProperty(value="是否外键，字典TUREORNOT 1是，0否",notes="长度为：10")
	private Integer isFk;
	/**
	* 关联类型，字典DMSDATAREFTYPE 1：1对1，2：1对多
	*/
	@ApiModelProperty(value="关联类型，字典DMSDATAREFTYPE 1：1对1，2：1对多",notes="长度为：10")
	private Integer fkType;
	/**
	* 关联表ID
	*/
	@ApiModelProperty(value="关联表ID",notes="长度为：19")
	private Long fkTableId;
	/**
	* 关联表名称
	*/
	@ApiModelProperty(value="关联表名称",notes="长度为：100")
	private String fkTableName;
	/**
	* 关联字段ID
	*/
	@ApiModelProperty(value="关联字段ID",notes="长度为：19")
	private Long fkFieldId;
	/**
	* 关联字段名称
	*/
	@ApiModelProperty(value="关联字段名称",notes="长度为：50")
	private String fkFieldName;
	/**
	* 关联显示字段名称
	*/
	@ApiModelProperty(value="关联显示字段名称",notes="长度为：50")
	private String fkLabelName;
	/**
	* 关联方式，字典DMSDATAREFWAY left：左关联，right：右关联，inner：内关联
	*/
	@ApiModelProperty(value="关联方式，字典DMSDATAREFWAY left：左关联，right：右关联，inner：内关联",notes="长度为：10")
	private String fkRefWay;
	/**
	* 是否可以为空，字典TUREORNOT 1是，0否
	*/
	@ApiModelProperty(value="是否可以为空，字典TUREORNOT 1是，0否",notes="长度为：10")
	private Integer isNull;
	/**
	* 是否唯一，唯一索引
	*/
	@ApiModelProperty(value="是否唯一，唯一索引",notes="长度为：10")
	private Integer isUnique;
	/**
	* 表单控件，字典DMSWIDGET
	*/
	@ApiModelProperty(value="表单控件，字典DMSWIDGET",notes="长度为：50")
	private String widgetName;
	/**
	* 是否查询，是否查询字段，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="是否查询，是否查询字段，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer isQuery;
	/**
	* 默认查询，字符类型，使用keywords关键字搜索，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="默认查询，字符类型，使用keywords关键字搜索，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer queryDefault;
	/**
	* 查询方式，字典DCF-QUERY-TYPE  EQ：精确查询，LIKE：模糊查询，LLIKE：左模糊，RLIKE：右模糊
	*/
	@ApiModelProperty(value="查询方式，字典DCF-QUERY-TYPE  EQ：精确查询，LIKE：模糊查询，LLIKE：左模糊，RLIKE：右模糊",notes="长度为：10")
	private String queryType;
	/**
	* 标准字段，关联的标准字段名称
	*/
	@ApiModelProperty(value="标准字段，关联的标准字段名称",notes="长度为：50")
	private String stsField;
	/**
	* 字段配置，JSON结构,{}
            {
                fkey:{外键配置，关联字段集合，列表显示控制等等},
                widget:{表单控件配置，默认值，是否只读},
                convert:{转换器配置},
                show:{表单显示配置}
            }
	*/
	@ApiModelProperty(value="字段配置，JSON结构,{}"+
            "{"+
            "    fkey:{外键配置，关联字段集合，列表显示控制等等},"+
            "    widget:{表单控件配置，默认值，是否只读},"+
            "    convert:{转换器配置},"+
            "    show:{表单显示配置}"+
            "}",notes="长度为：2147483647")
	private String configs;
	/**
	* 是否需要授权
	*/
	@ApiModelProperty(value="是否需要授权",notes="长度为：10")
	private Integer needAuth;
	/**
	* 同步开关，是否需要同步去到数据库，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="同步开关，是否需要同步去到数据库，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer syncEnable;
	/**
	* 同步状态，是否已同步数据库，字典TUREORFALSE 1是，0否
	*/
	@ApiModelProperty(value="同步状态，是否已同步数据库，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer syncFlag;
	/**
	* 显示顺序
	*/
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	/**
	* 使用状态，字典USEORNOT 1使用，0停用
	*/
	@ApiModelProperty(value="使用状态，字典USEORNOT 1使用，0停用",notes="长度为：10")
	private Integer status;
	/**
	* 说明
	*/
	@ApiModelProperty(value="说明",notes="长度为：500")
	private String descs;

}
