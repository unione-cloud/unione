package com.unione.cloud.beetsql;

import java.util.HashMap;
import java.util.Map;

import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Pojo;


/**
 * 	数据查询对象
 * @author Jeking Yang
 */
public class Finder<T> {

	private Map<String, Boolean> fields=new HashMap<>();
	private T data;
	private T params;
	private Sort[] sorts;
	
	private Finder(T data) {
		this.data=data;
		this.params=data;
	}
	
	private Finder(T data,Sort[] sorts) {
		this.data=data;
		this.params=data;
		this.sorts=sorts;
	}
	
	private Finder(T data,T params) {
		this.data=data;
		this.params=params;
	}
	
	/**
	 * 	构建Updater实例
	 * @param data
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Finder<T> build(T data) {
		return new Finder(data);
	}
	
	public static <T> Finder<T> build(T data,Sort[] sorts) {
		return new Finder(data);
	}
	
	/**
	 * 	构建Updater实例
	 * @param data
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <T> Finder<T> build(T data,T params) {
		return new Finder(data,params);
	}
	
	public Sort[] getSorts() {
		return sorts;
	}
	public void setSorts(Sort[] sorts) {
		this.sorts = sorts;
	}

	/**
	 * 	设置可以更新的字段集合
	 * @param field
	 * @return
	 */
	public Finder<T> fields(String... fields) {
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

	public Long getId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Finder params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getId();
	}
	public Long getTenantId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Finder params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getTenantId();
	}
	public Long getOrgId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Finder params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getOrgId();
	}
	public Long getUserId() {
		AssertUtil.service().isTrue(params instanceof Pojo, "Finder params实例对象类型必须是com.unione.cloud.core.model.Pojo");
		return ((Pojo)params).getUserId();
	}
}
