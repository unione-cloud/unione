package com.unione.cloud.form.data.storage.model;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.form.data.dto.DataDefineDto.DataFieldDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("数据定义对象")
public class DataDefine implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1324844926863322561L;
	@ApiModelProperty(value="主键",notes="长度为：19")
	private Long id;
	@ApiModelProperty(value="数据源ID",notes="长度为：19")
	private Long dsId;
	@ApiModelProperty(value="编码，唯一",notes="长度为：50")
	private String sn;
	@ApiModelProperty(value="数据标题",notes="长度为：100")
	private String title;
	@ApiModelProperty(value="数据名称，对应数据库表名称",notes="长度为：100")
	private String name;
	@ApiModelProperty(value="版本号",notes="长度为：10")
	private Integer vers;
	@ApiModelProperty(value="类型，字典DMSDATACATEGORY jdbc：数据库，api：Rest接口",notes="长度为：20")
	private String category;
	@ApiModelProperty(value="数据查询脚本",notes="长度为：65535")
	private String sqlFind;
	@ApiModelProperty(value="数据新增脚本",notes="长度为：65535")
	private String sqlInsert;
	@ApiModelProperty(value="数据更新脚本",notes="长度为：65535")
	private String sqlUpdate;
	@ApiModelProperty(value="数据删除脚本",notes="长度为：65535")
	private String sqlDelete;
	
	@ApiModelProperty(value="字段集合")
	private List<DataFieldDto> fields;
	
	
	/**
	 * 	获取指定标准字段对象
	 * @param field
	 * @return
	 */
	public DataFieldDto getStsField(BaseField field) {
		if(this.fields!=null) {
			Optional<DataFieldDto> optional = this.fields.stream()
				.filter(f->field.getColumn().equals(f.getName()) || field.getName().equals(f.getStsField()))
				.findFirst();
			if(optional.isPresent()) {
				return optional.get();
			}
		}
		return null;
	}
	
}
