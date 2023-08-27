package com.unione.cloud.core.exception;


/**
 * @描述 <p> 数据操作异常，可能需要进行实物回滚
 * @author Jeking Yang
 * @since 1.0.0
 */
public class DataBaseException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5685435016603963880L;

	/**
	 * 异常编号
	 */
	private String errorCode;
	
	/**
	 * 异常消息
	 */
	private String errorMessage;
	
	/**
	 * 异常处理对象
	 */
	private Object sources;

	/**
	 * 
	 */
	public DataBaseException() {
	}

	/**
	 * 异常方法
	 */
	public DataBaseException(String message) {
		super(message);
		this.errorMessage = message;
	}
	
	/**
	 * 异常方法
	 */
	public DataBaseException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 异常方法
	 */
	public DataBaseException(String code, String message) {
		super(message);
		this.errorCode = code;
		this.errorMessage = message;
	}
	
	/**
	 * 异常方法
	 */
	public DataBaseException(String message, Throwable cause) {
		super(message, cause);
		this.errorMessage = message;
	}

	/**
	 * 异常方法
	 */
	public DataBaseException(String code, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = code;
		this.errorMessage = message;
	}

	/**
	 * 异常方法
	 */
	public DataBaseException(String code, String message, Object sources, Throwable cause) {
		super(message, cause);
		this.errorCode = code;
		this.errorMessage = message;
		this.sources = sources;
	}

	// -----------------------------------------
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Object getSources() {
		return sources;
	}

	public void setSources(Object sources) {
		this.sources = sources;
	}
	

}
