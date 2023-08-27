package com.unione.cloud.core.exception;

/**
 * @描述 <p>系统服务层异常处理类.
 * @author Jeking Yang
 * @since 1.0.0
 */
public class ServiceException extends RuntimeException {
	
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
	private static final long serialVersionUID = 5564417708050358351L;

	/**
	 * 
	 */
	public ServiceException() {
	}

	/**
	 * 异常方法
	 */
	public ServiceException(String message) {
		super(message);
		this.errorMessage = message;
	}
	
	/**
	 * 异常方法
	 */
	public ServiceException(Throwable cause) {
		super(cause);
	}
	
	/**
	 * 异常方法
	 */
	public ServiceException(String code, String message) {
		super(message);
		this.errorCode = code;
		this.errorMessage = message;
	}
	
	/**
	 * 异常方法
	 */
	public ServiceException(String message, Throwable cause) {
		super(message, cause);
		this.errorMessage = message;
	}

	/**
	 * 异常方法
	 */
	public ServiceException(String code, String message, Throwable cause) {
		super(message, cause);
		this.errorCode = code;
		this.errorMessage = message;
	}

	/**
	 * 异常方法
	 */
	public ServiceException(String code, String message, Object sources, Throwable cause) {
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
