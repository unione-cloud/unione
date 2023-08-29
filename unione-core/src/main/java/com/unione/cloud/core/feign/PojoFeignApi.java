package com.unione.cloud.core.feign;

import com.unione.cloud.core.feign.api.FeignDelete;
import com.unione.cloud.core.feign.api.FeignDetail;
import com.unione.cloud.core.feign.api.FeignFind;
import com.unione.cloud.core.feign.api.FeignFindById;
import com.unione.cloud.core.feign.api.FeignSave;
import com.unione.cloud.core.feign.api.FeignUpdate;

/**
 * @标题 通用pojo feign api
 * @作者	Jeking Yang
 * @日期	2020-08-06
 * @版本	1.0.0
 */
public interface PojoFeignApi<T> extends FeignSave<T>,FeignUpdate<T>,FeignDelete<T>,FeignFind<T>,FeignFindById<T>,FeignDetail<T>{
	
	
}
