package com.unione.cloud.beetsql;

import java.util.HashMap;
import java.util.Map;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.security.UserPrincipal;


/**
 * 	数据更新对象
 * @author Jeking Yang
 */
public class Updater<T> {

	private Map<String, Boolean> fields=new HashMap<>();
	private T data;
	private T params;
	private UserPrincipal principal;
	
	private Updater(T data) {
		this.data=data;
		this.params=data;
	}
	
	private Updater(T data,T params) {
		this.data=data;
		this.params=params;
	}
	
	/**
	 * 	构建Updater实例
	 * @param data
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Updater<T> build(T data) {
		return new Updater(data);
	}
	
	/**
	 * 	构建Updater实例
	 * @param data
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Updater<T> build(T data,T params) {
		return new Updater(data,params);
	}
	
	/**
	 * 	设置可以更新的字段集合
	 * @param field
	 * @return
	 */
	public Updater<T> fields(String... fields) {
		for(String field:fields) {
			this.fields.put(field, true);
		}
		return this;
	}

	
	public Map<String, Boolean> getFields() {
		return fields;
	}
	public T getData() {
		return data;
	}
	public T getParams() {
		return params;
	}
	public UserPrincipal getPrincipal() {
		return principal;
	}
	public void setPrincipal(UserPrincipal principal) {
		this.principal = principal;
	}

	public Long getId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Updater params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getId();
	}
	public Long getTenantId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Updater params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getTenantId();
	}
	public Long getOrgId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Updater params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getOrgId();
	}
	public Long getUserId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Updater params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getUserId();
	}
}
