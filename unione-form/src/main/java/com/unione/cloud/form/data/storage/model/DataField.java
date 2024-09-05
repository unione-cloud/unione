package com.unione.cloud.form.data.storage.model;

import java.io.Serializable;

import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("数据字段DTO")
public class DataField implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1252249956114670620L;
	@ApiModelProperty(value="主键",notes="长度为：19")
	private Long id; 
	@ApiModelProperty(value="数据模型ID",notes="长度为：19")
	private Long modelId;
	@ApiModelProperty(value="标题",notes="长度为：100")
	private String title;
	@ApiModelProperty(value="名称",notes="长度为：50")
	private String name;
	@ApiModelProperty(value="数据类型，直接使用java映射类型，如：String，Double，Float，Boolean，Date 等",notes="长度为：20")
	private String dataType;
	@ApiModelProperty(value="数据格式",notes="长度为：50")
	private String dataFormat;
	@ApiModelProperty(value="数据长度",notes="长度为：10")
	private Integer dataLen;
	@ApiModelProperty(value="数据精度",notes="长度为：10")
	private Integer dataPrec;
	@ApiModelProperty(value="是否主键，字典TUREORNOT 1是，0否",notes="长度为：10")
	private Integer isPk;
	@ApiModelProperty(value="是否外键，字典TUREORNOT 1是，0否",notes="长度为：10")
	private Integer isFk;
	@ApiModelProperty(value="关联类型，字典DMSDATAREFTYPE 1：1对1，2：1对多",notes="长度为：10")
	private Integer fkType;
	@ApiModelProperty(value="关联表ID",notes="长度为：19")
	private Long fkTableId;
	@ApiModelProperty(value="关联表名称",notes="长度为：100")
	private String fkTableName;
	@ApiModelProperty(value="关联字段ID",notes="长度为：19")
	private Long fkFieldId;
	@ApiModelProperty(value="关联字段名称",notes="长度为：50")
	private String fkFieldName;
	@ApiModelProperty(value="关联显示字段名称",notes="长度为：50")
	private String fkLabelName;
	@ApiModelProperty(value="关联方式，字典DMSDATAREFWAY left：左关联，right：右关联，inner：内关联",notes="长度为：10")
	private String fkRefWay;
	@ApiModelProperty(value="是否可以为空，字典TUREORNOT 1是，0否",notes="长度为：10")
	private Integer isNull;
	@ApiModelProperty(value="是否查询，是否查询字段，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer isQuery;
	@ApiModelProperty(value="默认查询，字符类型，使用keywords关键字搜索，字典TUREORFALSE 1是，0否",notes="长度为：10")
	private Integer queryDefault;
	@ApiModelProperty(value="查询方式，字典DCF-QUERY-TYPE  EQ：精确查询，LIKE：模糊查询，LLIKE：左模糊，RLIKE：右模糊",notes="长度为：10")
	private String queryType;
	@ApiModelProperty(value="标准字段，关联的标准字段名称",notes="长度为：50")
	private String stsField;
	@ApiModelProperty(value="是否需要授权",notes="长度为：10")
	private Integer needAuth;
	@ApiModelProperty(value="显示顺序",notes="长度为：10")
	private Integer ordered;
	
	private DataFieldConfig configs;
	
	/**
	 * 	获取字段别名：column驼峰
	 * @return
	 */
	public String getAlias() {
		if(name!=null) {
			return StrUtil.toCamelCase(name);
		}
		return null;
	}
	
}
