package com.unione.cloud.pay.model;
import java.util.Date;
import org.beetl.sql.annotation.entity.*;
import org.beetl.sql.annotation.entity.Table;
import org.beetl.sql.mapper.annotation.SqlResource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.core.model.Pojo;

/**
 * @标题 	CommUpayOrder Entity
 * @描述	通用：支付订单
 * @作者	Unione Cloud CodeGen
 * @日期	2026-01-28 16:32:01
 * @版本	1.0.0
 **/
@Data
@Builder
@DataPermis(PermisRule.USERID)
@SqlResource("common.CommUpayOrder")
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name="comm_upay_order")
public class CommUpayOrder extends Pojo {
	/**
	* 订单标题
	*/
	@Schema(title="订单标题",description="长度为：100")
	private String title;
	/**
	* 业务ID
	*/
	@Schema(title="业务ID",description="长度为：19")
	private Long busiId;
	/**
	* 业务类型，字典UPAYBUSITYPE goods：商品订单，vip：会员订单，coin：金币订单，diamond：钻石订单
	*/
	@Schema(title="业务类型，字典UPAYBUSITYPE goods：商品订单，vip：会员订单，coin：金币订单，diamond：钻石订单",description="长度为：20")
	private String busiType;
	/**
	* 支付方式，字典UPAYWAY wx:微信，alipay：支付宝
	*/
	@Schema(title="支付方式，字典UPAYWAY wx:微信，alipay：支付宝",description="长度为：10")
	private String payWay;
	/**
	* 交易单号
	*/
	@Schema(title="交易单号",description="长度为：100")
	private String tradeid;
	/**
	* 交易商户号
	*/
	@Schema(title="交易商户号",description="长度为：50")
	private String mchid;
	/**
	* 支付金额（分）
	*/
	@Schema(title="支付金额（分）",description="长度为：10")
	private Integer payAmount;
	/**
	* 下单平台
	*/
	@Schema(title="下单平台",description="长度为：20")
	private String platform;
	/**
	* 订单状态，字典UPAYORDERSTATUS 1：待付款，2：已付款，3：退款审核中，4：退款中，5：已退款，-1：已取消付款/退款，-2：退款拒绝，-3：支付/退款失败
	*/
	@Schema(title="订单状态，字典UPAYORDERSTATUS 1：待付款，2：已付款，3：退款审核中，4：退款中，5：已退款，-1：已取消付款/退款，-2：退款拒绝，-3：支付/退款失败",description="长度为：10")
	private Integer status;
	/**
	* 是否退款订单，字典TRUEORFALSE 1是，0否
	*/
	@Schema(title="是否退款订单，字典TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer isRefund;
	/**
	* 退款金额（非退款订单表示已退款总金额，退款订单表示该笔退款的退款金额），单位：分
	*/
	@Schema(title="退款金额（非退款订单表示已退款总金额，退款订单表示该笔退款的退款金额），单位：分",description="长度为：10")
	private Integer refundAmount;
	/**
	* 总退款笔数（非退款订单有值）
	*/
	@Schema(title="总退款笔数（非退款订单有值）",description="长度为：10")
	private Integer refundCount;
	/**
	* 订单创建时间/发起退款时间
	*/
	@Schema(title="订单创建时间/发起退款时间",description="长度为：19")
	private Date createTime;
	/**
	* 订单更新时间
	*/
	@Schema(title="订单更新时间",description="长度为：19")
	private Date updateTime;
	/**
	* 订单付款时间
	*/
	@Schema(title="订单付款时间",description="长度为：19")
	private Date payTime;
	/**
	* 订单完成退款时间
	*/
	@Schema(title="订单完成退款时间",description="长度为：19")
	private Date refundTime;
	/**
	* 退款原因
	*/
	@Schema(title="退款原因",description="长度为：200")
	private String refundDesc;
	/**
	* 失败原因
	*/
	@Schema(title="失败原因",description="长度为：2000")
	private String errorTxt;
	/**
	* 自定义的订单拓展信息，如商品信息、会员卡种类信息等,JSON存储
	*/
	@Schema(title="自定义的订单拓展信息，如商品信息、会员卡种类信息等,JSON存储",description="长度为：2000")
	private String info;
	/**
	* 是否对账，字典TRUEORFALSE 1是，0否
	*/
	@Schema(title="是否对账，字典TRUEORFALSE 1是，0否",description="长度为：10")
	private Integer checkFlag;
	/**
	* 对账人ID
	*/
	@Schema(title="对账人ID",description="长度为：19")
	private Long checkUserId;
	/**
	* 对账人姓名
	*/
	@Schema(title="对账人姓名",description="长度为：50")
	private String checkUserName;
	/**
	* 对账时间
	*/
	@Schema(title="对账时间",description="长度为：19")
	private Date checkTime;
	/**
	* 描述
	*/
	@Schema(title="描述",description="长度为：500")
	private String descs;

}
