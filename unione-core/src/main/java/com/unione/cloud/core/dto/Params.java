package com.unione.cloud.core.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * @描述
 *     <p>
 *     系统请求DTO
 * 
 * @author Jeking Yang
 * @since 1.0.0
 */
@Data
@Slf4j
@Accessors(chain = true)
@ApiModel(description = "系統请求DTO，排序字段，排序方式都需要进行转换，不能直接输出到SQL")
public class Params<T> implements Serializable {

	/**
	 * 序列化编号.
	 */
	private static final long serialVersionUID = 3885702483311119528L;

	@ApiModelProperty(value = "分页大小",notes = "默认每页10条记录", example = "10")
	private int pageSize = 10;

	/**
	 * 总记录数
	 */
	@ApiModelProperty(hidden=true)
	private long total;

	@ApiModelProperty(value = "当前页", example = "1")
	private int page = 1;

	/**
	 * 当前页第一条数据在List中的位置,从0开始
	 */
	@JsonIgnore
	@ApiModelProperty(hidden=true)
	private int start;
	
	/**
	 * 当前页结束条数据在List中的位置,从0开始
	 */
	@JsonIgnore
	@ApiModelProperty(hidden=true)
	private int limit;
	
	@ApiModelProperty(value="是否需要count统计",notes = "前端查询条件无变化时，可以传入false，减少count统计时间消耗")
	private boolean needCount=true;
	
	/**
	 * 	查询操作数据权限开关，根据业务场景进行设置，默认关闭
	 */
	@JsonIgnore
	@ApiModelProperty(hidden=true)
	private boolean dtps;

	@ApiModelProperty(value = "排序字段", example = "id")
	private String sortName = "ID";

	@ApiModelProperty(value = "排序方式", example = "desc")
	private String sortOrder = "DESC";

	@ApiModelProperty("请求参数")
	private T body;

	/**
	 * 创建参数DTO实例
	 * @param <P>
	 * @param cls
	 * @return
	 */
	public static <P> Params<P> build(Class<P> cls){
		try {
			P body=(P)cls.getDeclaredConstructor().newInstance();
			Params<P> param=new Params<P>();
			param.setBody(body);
			return param;
		} catch (Exception e) {
			log.error("构建参数对象异常",e);
			throw new RuntimeException("构建参数对象异常");
		}
	}
	
	/**
	 * 创建参数DTO实例
	 * @param <P>
	 * @param body
	 * @return
	 */
	public static <P> Params<P> build(P body){
		Params<P> params=new Params<P>();
		params.setBody(body);
		return params;
	}

	/**
	 * @return the start
	 */
	public int getStart() {
		this.start = (page - 1) * pageSize;
		return this.start;
	}
	

	/**
	 * @return the limit
	 */
	public int getLimit() {
		this.limit = pageSize;
		return this.limit;
	}
	
	public void setSortName(String sortName) {
		if(sortName!=null && sortName.matches("[a-zA-Z,]*[^,]$")) {
			if(sortName.matches("[A-Z,]*[^,]$")) {
				this.sortName=sortName;
			}else {
				this.sortName=sortName.replaceAll("[A-Z]", "_$0").toUpperCase();
			}
		}
	}
	
	public void setSortOrder(String sortOrder) {
		if(sortOrder!=null && sortOrder.matches("^(?i)(desc|asc|,)$")) {
			this.sortOrder=sortOrder.toUpperCase();
		}
	}
	
	
}
