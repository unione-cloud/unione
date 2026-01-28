package com.unione.cloud.pay.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Validator;
import com.unione.cloud.pay.dto.UpayAppOrder;
import com.unione.cloud.pay.dto.UpayCreateOrder;
import com.unione.cloud.pay.model.CommUpayOrder;
import com.unione.cloud.pay.service.UpayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RefreshScope
@RestController
@Tag(name = "公共服务：统一支付")
@RequestMapping("/api/pay")
public class UpayController {

	@Autowired
	private DataBaseDao dataBaseDao;

	@Autowired
	private UpayService upayService;


	@PostMapping("/app/create")
	@Operation(summary = "创建订单")
	public Results<UpayAppOrder> appCreate(@RequestBody @Validated(Validator.save.class) UpayCreateOrder order) {
		UpayAppOrder info=upayService.createAppOrder(order);
		return Results.success(info);
	}


	@PostMapping("/pay/notify/{payWay}")
	@Operation(summary = "支付通知", description = "支付平台异步通知入口")
	public ResponseEntity<String> payNotify(@RequestBody String data,@PathVariable("payWay") String payWay) {
		return upayService.payNotify(data,payWay);
	}
	

	@PostMapping("/detail")
	@Operation(summary = "订单详情", description = "根据订单id获取订单详情")
	public Results<CommUpayOrder> detail(@RequestBody Long id) {
		AssertUtil.service().notNull(id, "参数id不能为空");

		CommUpayOrder tmp = dataBaseDao.findById(SqlBuilder.build(CommUpayOrder.class, id));
		AssertUtil.service().notNull(tmp, "记录未找到");

		return Results.success(tmp);
	}

	@PostMapping("/list")
	@Operation(summary = "订单列表", description = "更具业务id获取订单列表")
	public Results<List<CommUpayOrder>> list(@RequestBody Long id) {

		CommUpayOrder order = CommUpayOrder.builder().busiId(id).build();
		order.setDelFlag(0);
		List<CommUpayOrder> list = dataBaseDao.findList(SqlBuilder.build(order));

		return Results.success(list);
	}

	@PostMapping("/mine")
	@Operation(summary = "我的订单列表", description = "加载当前用户的订单列表")
	public Results<List<CommUpayOrder>> mine(@RequestBody Params<CommUpayOrder> params) {
		params.getBody().setDelFlag(0);
		Results<List<CommUpayOrder>> results = dataBaseDao.findPages(SqlBuilder.build(params));
		return results;
	}

}
