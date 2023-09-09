package com.unione.cloud.core.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @描述 <p>分页数据响应DTO
 * 
 * @author Jeking Yang
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
@ApiModel(description = "系统响应DTO[分页]")
public class Results<T> implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -625603360696666874L;

	/**
	 * 操作结果
	 */
	@ApiModelProperty("操作结果")
	private boolean success;

	/**
	 * 结果编码
	 */
	@ApiModelProperty("结果编码")
	private Integer code;

	/**
	 * 响应消息
	 */
	@ApiModelProperty("响应消息")
	private String message;

	/**
	 * 响应数据
	 */
	@ApiModelProperty("响应数据")
	private T body;
	
	/**
	 * 分页大小
	 */
	@ApiModelProperty("分页大小")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer pageSize;

	/**
	 * 记录总数
	 */
	@ApiModelProperty("记录总数")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long total;

	/**
	 * 当前页
	 */
	@ApiModelProperty("当前页")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer page;
	
	
	/**
	 * 	构建响应
	 * @param <T>
	 * @param isSuccess
	 * @return
	 */
	public static <T> Results<T> build(boolean isSuccess){
		Results<T> Results=new Results<>();
		Results.setSuccess(isSuccess);
		Results.setMessage(isSuccess?"操作成功":"操作失败");
		Results.setCode(200);
		return Results;
	}

	/**
	 * 	构建响应
	 * @param <T>
	 * @param isSuccess
	 * @return
	 */
	public static <T> Results<T> build(boolean isSuccess, T body){
		Results<T> Results=new Results<>();
		Results.setSuccess(isSuccess);
		Results.setBody(isSuccess?body:null);
		Results.setMessage(isSuccess?"操作成功":"操作失败");
		Results.setCode(200);
		return Results;
	}
	
	/**
	 * 成功响应
	 * @param <T>
	 * @param body
	 * @param message
	 * @return
	 */
	public static <T> Results<T> success(T body,String message){
		Results<T> Results=new Results<>();
		Results.setBody(body);
		Results.setSuccess(true);
		Results.setMessage(message);
		Results.setCode(200);
		return Results;
	}
    	
	/**
	 * 	成功响应
	 * @param <T>
	 * @param body
	 * @return
	 */
	public static <T> Results<T> success(T body){
		Results<T> Results=new Results<>();
		Results.setBody(body);
		Results.setSuccess(true);
		Results.setMessage("操作成功");
		Results.setCode(200);
		return Results;
	}
	
	/**
	 * 	成功响应
	 * @param <T>
	 * @return
	 */
	public static <T> Results<T> success(){
		Results<T> Results=new Results<>();
		Results.setSuccess(true);
		Results.setMessage("操作成功");
		Results.setCode(200);
		return Results;
	}
	
	/**
	 * 	失败响应
	 * @param <T>
	 * @param body
	 * @return
	 */
	public static <T> Results<T> failure(T body){
		Results<T> Results=new Results<>();
		Results.setBody(body);
		Results.setSuccess(false);
		Results.setMessage("操作失败");
		Results.setCode(200);
		return Results;
	}
	
	/**
	 * 失败响应
	 * @param <T>
	 * @param body
	 * @param message
	 * @return
	 */
	public static <T> Results<T> failure(T body,String message){
		Results<T> Results=new Results<>();
		Results.setBody(body);
		Results.setSuccess(false);
		Results.setMessage(message);
		Results.setCode(200);
		return Results;
	}
	
	/**
	 * 	失败响应
	 * @param message
	 * @return
	 */
	public static <T> Results<T> failure(String message){
		Results<T> Results=new Results<>();
		Results.setSuccess(false);
		Results.setMessage(message);
		Results.setCode(200);
		return Results;
	}
	
	/**
	 * 	失败响应
	 * @param <T>
	 * @return
	 */
	public static <T> Results<T> failure(){
		Results<T> Results=new Results<>();
		Results.setSuccess(false);
		Results.setMessage("操作失败");
		Results.setCode(200);
		return Results;
	}
	
	/**
	 * 	错误响应
	 * @param <T>
	 * @param body
	 * @return
	 */
	public static <T> Results<T> error(T body){
		Results<T> Results=new Results<>();
		Results.setBody(body);
		Results.setSuccess(false);
		Results.setMessage("系统异常");
		Results.setCode(500);
		return Results;
	}
	
	/**
	 * 错误响应
	 * @param <T>
	 * @param body
	 * @param message
	 * @return
	 */
	public static <T> Results<T> error(T body,String message){
		Results<T> Results=new Results<>();
		Results.setBody(body);
		Results.setSuccess(false);
		Results.setMessage(message);
		Results.setCode(500);
		return Results;
	}
	
	/**
	 * 	错误响应
	 * @param <T>
	 * @param body
	 * @return
	 */
	public static <T> Results<T> error(String message){
		Results<T> Results=new Results<>();
		Results.setSuccess(false);
		Results.setMessage(message);
		Results.setCode(500);
		return Results;
	}
	
	/**
	 * 	错误响应
	 * @param <T>
	 * @return
	 */
	public static <T> Results<T> error(){
		Results<T> Results=new Results<>();
		Results.setSuccess(false);
		Results.setMessage("系统异常");
		Results.setCode(500);
		return Results;
	}
	
	/**
	 * 	设置响应消息
	 * @param message
	 * @return
	 */
	public Results<T> setMessage(String message){
		this.message=message;
		return this;
	}
	
}
