package com.unione.cloud.form.page.dto;

import java.io.Serializable;
import java.util.List;

import com.unione.cloud.form.page.dto.TableWidgetDto.TableWidgetConfig;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("表格组件配置Dto")
public class TableWidgetDto extends WidgetDefineDto<TableWidgetConfig> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3079809143371203058L;
	
	
	@ApiModelProperty(value="数据模型编码",notes = "如果不为空，子组件不单独设置，则跟随父组件绑定同一个数据模型")
	private String dsn;
	
	@ApiModelProperty(value="表格列模型")
	private List<Object> columns;
	
	@ApiModelProperty(value="分页配置")
	private TablePaginationDto pagination;
	
	@ApiModelProperty(value="是否开启复选框")
	private boolean selection;
	
	@ApiModelProperty(value="左侧按钮列表",notes = "列表左侧按钮列表")
	private List<ButtonDefineDto> leftBtns;
	
	@ApiModelProperty(value="右侧按钮列表",notes = "右表左侧按钮列表")
	private List<ButtonDefineDto> rightBtns;
	
	@ApiModelProperty("行号设置")
	private TableRownumDto rownum;
	
	@ApiModelProperty("操作设置")
	private TableOperationDto operation;
	
	
	
	@Data
	@ApiModel("表格分页配置DTO")
	public static class TablePaginationDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1333732908409635861L;

		@ApiModelProperty("自定义记录总数显示逻辑")
		private String showTotalScript;
		
		@ApiModelProperty(value="每页记录数量",notes = "默认：10")
		private Integer pageSize;
		
		@ApiModelProperty(value="指定每页可以显示多少条",notes = "eg：['10', '20', '50', '100']")
		private List<Integer> pageSizeOptions;
		
		@ApiModelProperty(value="显示位置",notes = "")
		private String position;
		
		@ApiModelProperty(value = "分页大小",notes = "可选:large | middle | small")
		private String   size;
		
		@ApiModelProperty(value="当 size 未指定时，根据屏幕宽度自动调整尺寸",notes = "")
		private boolean responsive;
		
		@ApiModelProperty(value="当添加该属性时，显示为简单分页",notes = "")
		private boolean simple;
	}
	
	
	@Data
	@ApiModel("表格行号配置DTO")
	public static class TableRownumDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 8764883326870087401L;

		@ApiModelProperty(value="显示标题",notes = "默认：序号")
		private String title;
		
		@ApiModelProperty(value="固定方式",notes = "默认：left,可选：'left' | 'right'")
		private String fixed;
		
		@ApiModelProperty(value="对齐方式",notes = "默认：center,可选：'center' | 'left' | 'right'")
		private String align;
		
		@ApiModelProperty("显示宽度")
		private Integer width;
		
		@ApiModelProperty("是否显示")
		private boolean visible=true;
	}
	
	@Data
	@ApiModel("表格行号配置DTO")
	public static class TableOperationDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 296826468569608136L;

		@ApiModelProperty(value="显示标题",notes = "默认：操作")
		private String title;
		
		@ApiModelProperty(value="固定方式",notes = "默认：right,可选：'left' | 'right'")
		private String fixed;
		
		@ApiModelProperty(value="对齐方式",notes = "默认：center,可选：'center' | 'left' | 'right'")
		private String align;
		
		@ApiModelProperty(value="显示位置",notes = "默认最后面")
		private Integer index;
		
		@ApiModelProperty("显示宽度")
		private Integer width;
		
		@ApiModelProperty("显示操作数量")
		private Integer count;
		
		@ApiModelProperty("是否显示")
		private boolean visible=true;
		
		@ApiModelProperty("操作按钮列表")
		private List<ButtonDefineDto> btns;
		
		@ApiModelProperty("更多设置")
		private MoreOperationDto more;
	}
	
	@Data
	@ApiModel("更多操作配置DTO")
	public static class MoreOperationDto implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5609162190698708400L;

		@ApiModelProperty(value="标题",notes = "默认：更多")
		private String title;
		
		@ApiModelProperty(value="触发方式",notes = "默认：hover，可选：click|hover|contextmenu")
		private String trigger;
		
		@ApiModelProperty(value = "按钮size大小",notes = "可选：large | middle | small")
		private String size;
		
		@ApiModelProperty(value = "显示布局",notes = "可选：vertical | horizontal")
		private String layout;
		
	}
	
	
	@Data
	@ApiModel("表格组件配置DTo")
	public static class TableWidgetConfig implements Serializable{/**
		 * 
		 */
		private static final long serialVersionUID = -8092409023579026643L;
		
		
	}

}
