package com.unione.cloud.form.page.dto;

import com.unione.cloud.form.page.dto.TablePageDto.TablePageConfigDto;
import com.unione.cloud.form.page.dto.PageDefineDto.PageConfigDto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("列表页面定义DTO")
public class TablePageDto extends PageDefineDto<TablePageConfigDto>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4012564560375359255L;
	
	
	@Data
	@ApiModel("列表页面配置DTO")
	public static class TablePageConfigDto extends PageConfigDto{
		
		
		
		
	}

}
