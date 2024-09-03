package com.unione.cloud.form.data.storage.dto;

import com.unione.cloud.core.dto.Results;

public class DataResult<T> extends Results<T> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7550726584804583822L;

	/**
	 * 	构建响应
	 * @param <T>
	 * @param isSuccess
	 * @return
	 */
	public static <T> DataResult<T> build(boolean isSuccess){
		DataResult<T> result=new DataResult<>();
		result.setSuccess(isSuccess);
		result.setMessage(isSuccess?"操作成功":"操作失败");
		result.setCode(200);
		return result;
	}

	/**
	 * 	构建响应
	 * @param <T>
	 * @param isSuccess
	 * @return
	 */
	public static <T> DataResult<T> build(boolean isSuccess, T body){
		DataResult<T> result=new DataResult<>();
		result.setSuccess(isSuccess);
		result.setBody(isSuccess?body:null);
		result.setMessage(isSuccess?"操作成功":"操作失败");
		result.setCode(200);
		return result;
	}
	
	/**
	 * 成功响应
	 * @param <T>
	 * @param body
	 * @param message
	 * @return
	 */
	public static <T> DataResult<T> success(T body,String message){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> success(T body){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> success(){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> failure(T body){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> failure(T body,String message){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> failure(String message){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> failure(){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> error(T body){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> error(T body,String message){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> error(String message){
		DataResult<T> result=new DataResult<>();
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
	public static <T> DataResult<T> error(){
		DataResult<T> result=new DataResult<>();
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
	public DataResult<T> setMessage(String message){
		this.setMessage(message);
		return this;
	}
	
}
