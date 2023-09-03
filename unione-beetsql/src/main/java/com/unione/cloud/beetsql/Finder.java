package com.unione.cloud.beetsql;

import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Pojo;

import lombok.Data;


/**
 * 	数据查询对象
 * @author Jeking Yang
 */
@Data
public class Finder<T> {

	private String[] fields;
	private T params;
	private Where where;
	private Sort[] sorts;
	private String group;
	private String having;
	
	private long pageSize = 10;
	private long page = 1;
	
	private Finder(T params) {
		this.params=params;
	}
	
	/**
	 * 	构建Finder实例
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Finder<T> build(T params) {
		return new Finder(params);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> Finder<T> build(Params<T> params) {
		Finder<T> finder=new Finder(params.getBody());
		finder.setPage(params.getPage());
		finder.setPageSize(params.getPageSize());
		return finder;
	}
	
	public Finder<T> field(String... fields){
		this.fields=fields;
		return this;
	} 
	
	public Finder<T> where(String where){
		this.where=new Where(params,where);
		if(this.group!=null) {
			this.where.group(this.group);
		}
		if(this.having!=null) {
			this.where.having(this.having);
		}
		return this;
	}
	
	public Finder<T> group(String group){
		this.group=group.trim();
		if(this.where!=null) {
			this.where.group(group);
		}
		return this;
	}
	
	public Finder<T> having(String having){
		this.having=having.trim();
		if(this.where!=null) {
			this.where.having(having);
		}
		return this;
	}
	
	public long getStart() {
		return (page - 1) * pageSize;
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
