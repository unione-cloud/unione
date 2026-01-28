package com.unione.cloud.pay.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class UpayAppOrder implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String sign;
	private String prepayId;
	private String partnerId;
	private String appId;
	@JsonProperty("package")
	private String packageValue;
	private String timeStamp;
	private String nonceStr;

}
