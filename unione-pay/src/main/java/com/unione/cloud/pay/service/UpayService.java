package com.unione.cloud.pay.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Response;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyV3Result.DecryptNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderV3Request.Amount;
import com.github.binarywang.wxpay.bean.result.WxPayUnifiedOrderV3Result.AppResult;
import com.github.binarywang.wxpay.bean.result.enums.TradeTypeEnum;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.core.util.SpringCtxUtil;
import com.unione.cloud.pay.dto.UpayAppOrder;
import com.unione.cloud.pay.dto.UpayCreateOrder;
import com.unione.cloud.pay.model.CommUpayOrder;
import com.unione.cloud.pay.process.UpayProcess;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UpayService {

	@Autowired
	private WxPayService wxPayService;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private DataBaseDao dataBaseDao;

	/**
	 * 创建本地订单
	 * 
	 * @param order
	 * @return
	 */
	public CommUpayOrder createLocal(UpayCreateOrder create) {

		CommUpayOrder order = BeanUtils.copyProperties(create, CommUpayOrder.class);
		order.setStatus(1);
		order.setMchid(wxPayService.getConfig().getMchId());
		order.setCreateTime(DateUtil.date());
		order.setUpdateTime(DateUtil.date());
		order.setIsRefund(0);
		order.setDelFlag(0);
		dataBaseDao.insert(order);

		return order;
	}

	/**
	 * 创建订单
	 * 
	 * @param order
	 * @return
	 */
	public UpayAppOrder createAppOrder(UpayCreateOrder create) {

		try {
			// 创建本地订单
			CommUpayOrder local = this.createLocal(create);

			if ("wx".equals(create.getPayWay())) {
				WxPayUnifiedOrderV3Request orderRequest = new WxPayUnifiedOrderV3Request();
				orderRequest.setDescription(create.getDescs());
				orderRequest.setOutTradeNo(String.valueOf(local.getId()));
				Amount amount = new Amount();
				amount.setTotal(create.getPayAmount());
				orderRequest.setAmount(amount);
				orderRequest.setAttach(String.valueOf(create.getBusiId()));
				// 调用统一下单接口
				AppResult result = wxPayService.createOrderV3(TradeTypeEnum.APP, orderRequest);
				return BeanUtils.toBean(result, UpayAppOrder.class);
			}

		} catch (WxPayException e) {
			log.error("APP端创建支付订单失败，微信支付下单失败");
			throw new ServiceException("APP端创建支付订单失败，微信支付下单失败", e);
		} catch (Exception e) {
			log.error("APP端创建支付订单失败，系统异常");
			throw new ServiceException("APP端创建支付订单失败，系统异常");
		}

		return null;
	}

	/**
	 * 支付回调通知处理
	 * 
	 * @param data
	 * @param payWay
	 * @return
	 */
	public ResponseEntity<String> payNotify(String data, String payWay) {
		CommUpayOrder local = null;
		try {
			if ("wx".equals(payWay)) {
				WxPayNotifyV3Result result = wxPayService.parseOrderNotifyV3Result(data, getRequestHeader(request));
				DecryptNotifyResult decrypt = result.getResult();
				String outTradeNo = decrypt.getOutTradeNo();
				if (ObjectUtil.isEmpty(outTradeNo) || !outTradeNo.matches("\\d+")) {
					log.error("outTradeNo丢失或格式不正确，无法关联本地订单,outTradeNo:{}",decrypt.getOutTradeNo());
					return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("outTradeNo为空或格式不正确"));
				}
				Long id=Long.parseLong(outTradeNo);
				local = dataBaseDao.findById(SqlBuilder.build(CommUpayOrder.class,id));
				if (ObjectUtil.isEmpty(local)) {
					log.error("outTradeNo关联的本地订单不存在,outTradeNo:{}",outTradeNo);
					return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("outTradeNo关联的本地订单不存在"));
				}

				if (WxPayConstants.WxpayTradeStatus.SUCCESS.equals(decrypt.getTradeState())) {
					// 支付成功
					local.setStatus(2);
					local.setPayTime(DateUtil.parse(decrypt.getSuccessTime()));
					local.setTradeid(decrypt.getTransactionId());
					local.setUpdateTime(DateUtil.date());
					int len = dataBaseDao.updateById(SqlBuilder.build(local).field("status,payTime,tradeid,updateTime").dataPermis(PermisRule.ALL));
					if(len<=0){
						log.error("更新本地订单支付成功信息失败,outTradeNo:{},id:{}",outTradeNo,id);
						return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("更新本地订单支付成功信息失败"));
					}
					
					// 支付成功处理
					this.process(local, true);
					return ResponseEntity.status(200).body(WxPayNotifyV3Response.success("支付成功"));
				} else {
					// 支付失败
					local.setStatus(-3);
					StringBuffer error=new StringBuffer(local.getErrorTxt());
					error.append("\n======================");
					error.append("\n微信支付返回错误,错误信息：").append(decrypt.getTradeStateDesc());
					local.setErrorTxt(error.toString());
					int len = dataBaseDao.updateById(SqlBuilder.build(local).field("status,errorTxt").dataPermis(PermisRule.ALL));
					if(len<=0){
						log.error("更新本地订单支付失败信息失败,outTradeNo:{},id:{}",outTradeNo,id);
						return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("更新本地订单支付失败信息失败"));
					}

					// 支付失败处理
					this.process(local, false);
					return ResponseEntity.status(200).body(WxPayNotifyV3Response.success("支付失败处理成功"));
				}
			}
		} catch (Exception e) {
			log.error("支付回调通知处理失败，系统异常");
			return ResponseEntity.status(500).body(WxPayNotifyV3Response.fail("支付回调处理异常"));
		}
		return null;
	}

	/**
	 * 组装请求头重的前面信息
	 *
	 * @param request
	 * @return
	 */
	private SignatureHeader getRequestHeader(HttpServletRequest request) {
		// 获取通知签名
		String signature = request.getHeader("Wechatpay-Signature");
		String nonce = request.getHeader("Wechatpay-Nonce");
		String serial = request.getHeader("Wechatpay-Serial");
		String timestamp = request.getHeader("Wechatpay-Timestamp");

		SignatureHeader signatureHeader = new SignatureHeader();
		signatureHeader.setSignature(signature);
		signatureHeader.setNonce(nonce);
		signatureHeader.setSerial(serial);
		signatureHeader.setTimeStamp(timestamp);
		return signatureHeader;
	}

	/**
	 * 订单支付结果处理
	 * 
	 * @param order
	 * @param success
	 */
	public void process(CommUpayOrder order, boolean success) {
		try{
			UpayProcess process=SpringCtxUtil.getBean(String.format("%sUpayProcess", order.getBusiType()), UpayProcess.class);
			if(ObjectUtil.isEmpty(process)){
				return;
			}
			ThreadUtil.execute(new Runnable() {
				@Override
				public void run() {
					process.process(order, success);
				}
			});
		}catch(Exception e){
			log.error("订单支付结果处理失败,busitype:{},orderid:{}",order.getBusiType(),order.getId(),e);
		}
	}

}
