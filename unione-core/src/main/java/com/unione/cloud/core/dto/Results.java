package com.unione.cloud.core.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "系统响应DTO[分页]")
public class Results<T> implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -625603360696666874L;

	/**
	 * 操作结果
	 */
	@Schema(title="操作结果")
	private boolean success;

	/**
	 * 结果编码
	 */
	@Schema(title="结果编码")
	private Integer code;

	/**
	 * 响应消息
	 */
	@Schema(title="响应消息")
	private String message;

	/**
	 * 响应数据
	 */
	@Schema(title="响应数据")
	private T body;
	
	/**
	 * 分页大小
	 */
	@Schema(title="分页大小")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer pageSize;

	/**
	 * 记录总数
	 */
	@Schema(title="记录总数")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long total;

	/**
	 * 当前页
	 */
	@Schema(title="当前页")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer page;
	
	
	/**
	 * 	构建响应
	 * @param <T>
	 * @param isSuccess
	 * @return
	 */
	public static <T> Results<T> build(boolean isSuccess){
		Results<T> result=new Results<>();
		result.setSuccess(isSuccess);
		result.setMessage(isSuccess?"操作成功":"操作失败");
		result.setCode(isSuccess?200:500);
		return result;
	}

	/**
	 * 	构建响应
	 * @param <T>
	 * @param isSuccess
	 * @return
	 */
	public static <T> Results<T> build(boolean isSuccess, T body){
		Results<T> result=new Results<>();
		result.setSuccess(isSuccess);
		result.setBody(isSuccess?body:null);
		result.setMessage(isSuccess?"操作成功":"操作失败");
		result.setCode(isSuccess?200:500);
		return result;
	}
	
	/**
	 * 成功响应
	 * @param <T>
	 * @param body
	 * @param message
	 * @return
	 */
	public static <T> Results<T> success(T body,String message){
		Results<T> result=new Results<>();
		result.setBody(body);
		result.setSuccess(true);
		result.setMessage(message);
		result.setCode(200);
		return result;
	}
    	
	/**
	 * 	成功响应
	 * @param <T>
	 * @param body
	 * @return
	 */
	public static <T> Results<T> success(T body){
		Results<T> result=new Results<>();
		result.setBody(body);
		result.setSuccess(true);
		result.setMessage("操作成功");
		result.setCode(200);
		return result;
	}
	
	/**
	 * 	成功响应
	 * @param <T>
	 * @return
	 */
	public static <T> Results<T> success(){
		Results<T> result=new Results<>();
		result.setSuccess(true);
		result.setMessage("操作成功");
		result.setCode(200);
		return result;
	}
	
	/**
	 * 	失败响应
	 * @param <T>
	 * @param body
	 * @return
	 */
	public static <T> Results<T> failure(T body){
		Results<T> result=new Results<>();
		result.setBody(body);
		result.setSuccess(false);
		result.setMessage("操作失败");
		result.setCode(200);
		return result;
	}
	
	/**
	 * 失败响应
	 * @param <T>
	 * @param body
	 * @param message
	 * @return
	 */
	public static <T> Results<T> failure(T body,String message){
		Results<T> result=new Results<>();
		result.setBody(body);
		result.setSuccess(false);
		result.setMessage(message);
		result.setCode(200);
		return result;
	}
	
	/**
	 * 	失败响应
	 * @param message
	 * @return
	 */
	public static <T> Results<T> failure(String message){
		Results<T> result=new Results<>();
		result.setSuccess(false);
		result.setMessage(message);
		result.setCode(200);
		return result;
	}
	
	/**
	 * 	失败响应
	 * @param <T>
	 * @return
	 */
	public static <T> Results<T> failure(){
		Results<T> result=new Results<>();
		result.setSuccess(false);
		result.setMessage("操作失败");
		result.setCode(200);
		return result;
	}
	
	/**
	 * 	错误响应
	 * @param <T>
	 * @param body
	 * @return
	 */
	public static <T> Results<T> error(T body){
		Results<T> result=new Results<>();
		result.setBody(body);
		result.setSuccess(false);
		result.setMessage("系统异常");
		result.setCode(500);
		return result;
	}
	
	/**
	 * 错误响应
	 * @param <T>
	 * @param body
	 * @param message
	 * @return
	 */
	public static <T> Results<T> error(T body,String message){
		Results<T> result=new Results<>();
		result.setBody(body);
		result.setSuccess(false);
		result.setMessage(message);
		result.setCode(500);
		return result;
	}
	
	/**
	 * 	错误响应
	 * @param <T>
	 * @param body
	 * @return
	 */
	public static <T> Results<T> error(String message){
		Results<T> result=new Results<>();
		result.setSuccess(false);
		result.setMessage(message);
		result.setCode(500);
		return result;
	}
	
	/**
	 * 	错误响应
	 * @param <T>
	 * @return
	 */
	public static <T> Results<T> error(){
		Results<T> result=new Results<>();
		result.setSuccess(false);
		result.setMessage("系统异常");
		result.setCode(500);
		return result;
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
