package com.unione.cloud.pay.process;

import com.unione.cloud.pay.model.CommUpayOrder;

public interface UpayProcess {

    /**
     * 支付处理
     * @param order     支付订单
     * @param success   是否支付成功
     */
    public void process(CommUpayOrder order, boolean success);

}
