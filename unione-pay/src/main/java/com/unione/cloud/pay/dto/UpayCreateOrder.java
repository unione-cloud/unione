package com.unione.cloud.pay.dto;

import com.unione.cloud.core.model.Validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpayCreateOrder {
	/**
	* 订单标题
	*/
	@NotNull(message = "订单标题不能为空",groups = {Validator.save.class})
	@NotBlank(message = "订单标题不能为空",groups = {Validator.save.class})
	@Schema(title="订单标题",description="长度为：100")
	private String title;
	/**
	* 业务ID
	*/
	@NotNull(message = "业务ID不能为空",groups = {Validator.save.class})
	@Schema(title="业务ID",description="长度为：19")
	private Long busiId;
	/**
	* 业务类型，字典UPAYBUSITYPE goods：商品订单，vip：会员订单，coin：金币订单，diamond：钻石订单
	*/
	@NotNull(message = "业务类型不能为空",groups = {Validator.save.class})
	@NotBlank(message = "业务类型不能为空",groups = {Validator.save.class})
	@Schema(title="业务类型，字典UPAYBUSITYPE goods：商品订单，vip：会员订单，coin：金币订单，diamond：钻石订单",description="长度为：20")
	private String busiType;
	/**
	* 支付方式，字典UPAYWAY wx:微信，alipay：支付宝
	*/
	@NotNull(message = "支付方式不能为空",groups = {Validator.save.class})
	@NotBlank(message = "支付方式不能为空",groups = {Validator.save.class})
	@Schema(title="支付方式，字典UPAYWAY wx:微信，alipay：支付宝",description="长度为：10")
	private String payWay;
	/**
	* 支付金额（分）
	*/
	@Schema(title="支付金额（分）",description="长度为：10")
	@NotNull(message = "支付金额不能为空",groups = {Validator.save.class})
	@NotBlank(message = "支付金额不能为空",groups = {Validator.save.class})
	private Integer payAmount;
	/**
	* 下单平台
	*/
	@NotNull(message = "下单平台不能为空",groups = {Validator.save.class})
	@NotBlank(message = "下单平台不能为空",groups = {Validator.save.class})
	@Schema(title="下单平台",description="长度为：20")
	private String platform;
	/**
	* 自定义的订单拓展信息，如商品信息、会员卡种类信息等,JSON存储
	*/
	@Schema(title="自定义的订单拓展信息，如商品信息、会员卡种类信息等,JSON存储",description="长度为：2000")
	private String info;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;
}
