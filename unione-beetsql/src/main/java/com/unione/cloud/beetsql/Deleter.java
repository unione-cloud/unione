package com.unione.cloud.beetsql;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Pojo;


/**
 * 	数据删除对象
 * @author Jeking Yang
 */
public class Deleter<T> {

	private T params;
	
	private Deleter(T params) {
		this.params=params;
	}
	
	/**
	 * 	构建Deleter实例
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Deleter<T> build(T params) {
		return new Deleter(params);
	}
	
	public T getParams() {
		return params;
	}

	public Long getId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Deleter params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getId();
	}
	public Long getTenantId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Deleter params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getTenantId();
	}
	public Long getOrgId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Deleter params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getOrgId();
	}
	public Long getUserId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Deleter params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getUserId();
	}
}
