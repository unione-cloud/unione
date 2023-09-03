package com.unione.cloud.beetsql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.annotation.entity.Table;

import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Pojo;
import com.unione.cloud.core.security.UserPrincipal;

import cn.hutool.core.util.ArrayUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * 	SQL构建对象
 * @author Jeking Yang
 */
@Slf4j
public class SqlBuilder<T> {

	private String[] fieldList;				// 数据查询/更新字段
	@Getter
	private Map<String, String> fields; 	// 字段map集合
	@Getter
	private T data;				// 更新操作的数据对象
	@Getter
	private T params;			// 查询，更新，删除的过滤条件
	
	private Long   sourceId;	// 为空，默认数据源
	private String tableName;	// 数据表名称
	
	private String where;		// 数据过滤条件定义
	private Sort[] sorts;		// 数据排序
	private String group;		// 数据分组
	private String having;		// 分组处理
	
	@Getter
	private boolean needCount=true;
	
	private String countSql;
	private String findSql;
	private String whereSql;
	private String sql;
	@Getter
	private List<Object> values=new ArrayList<Object>();
	
	@Getter
	private long pageSize = 10;
	@Getter
	private long page = 1;
	
	private SqlBuilder(T params) {
		this.params=params;
		this.data=params;
		Table table = params.getClass().getAnnotation(Table.class);
		if(table!=null) {
			this.tableName=table.name();
		}
	}
	
	private SqlBuilder(T data,T params) {
		this.params=params;
		this.data=data;
		Table table = params.getClass().getAnnotation(Table.class);
		if(table!=null) {
			this.tableName=table.name();
		}
	}
	
	/**
	 * 	构建Finder实例
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(T params) {
		return new SqlBuilder(params);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(T data,T params) {
		return new SqlBuilder(data,params);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(Params<T> params) {
		SqlBuilder<T> buildr=new SqlBuilder(params.getBody());
		buildr.page(params.getPage()).pageSize(params.getPageSize());
		return buildr;
	}
	
	
	public Class<?> targetClass(){
		return this.data.getClass();
	}
	
	public String countSql() {
		this.process();
		
		if(this.countSql==null) {
			this.countSql=String.format("SELECT COUNT(*) FROM %s %s ",this.tableName,this.whereSql);
		}
		return this.countSql;
	}
	
	public String findSql() {
		this.process();
		
		if(this.findSql==null) {
			String selectField="*";
			if(this.fieldList!=null && this.fieldList.length>0) {
				selectField=ArrayUtil.join(this.fieldList, ",");
			}
			this.findSql=String.format("SELECT %s FROM %s %s ",selectField,this.tableName,this.whereSql);
		}
		return this.findSql;
	}
	
	public String updateSql() {
		
		return this.sql;
	}
	
	/**
	 * Sql Where 处理
	 */
	private void process() {
		if(this.whereSql!=null) {
			return;
		}
		if(StringUtils.isEmpty(this.where)) {
			this.whereSql="";
		}
		
		// (name=? and age>? and realname like ?) and (time>#{timeBegin} or time<=#{timeEnd})
		log.info("SQL 查询条件处理 where:{}",where);
		
		// 思路： 正则替换成beetl函数处理，
		// 如：(uniWhere("name=?") and uniWhere("age>?") and uniWhere("realname like ?") ) and ( uniWhere("time>#{timeBegin}") or uniWhere("time<=#{timeEnd}"))
		Pattern pattern=Pattern.compile("[\\w]+[\\s]*(=|>|>=|<|<=|like|LIKE|rlike|RLIKE|llike|LLIKE)[\\s]*(\\?|#\\{[\\s]*\\w*[\\s]*\\})");
		Matcher matcher=pattern.matcher(this.where);
		
		this.whereSql=this.where;
		while(matcher.find()) {
			this.whereSql=this.whereSql.replace(matcher.group(), "uniWhere(\""+matcher.group()+"\")");
		}
	}
	
	public SqlBuilder<T> field(String... fieldList){
		String[] list=fieldList;
		if(fieldList.length==1 && fieldList[0].indexOf(",")>0) {
			list=fieldList[0].replaceAll("\\s", "").split(",");
		}
		this.fields=new HashMap<>();
		this.fieldList=new String[list.length];
		for(int i=0;i<list.length;i++) {
			String field=list[i]; 
			String colName=field.replaceAll("[A-Z]", "_$0").toUpperCase();
			this.fields.put(field,colName);
			this.fieldList[i]=colName;
		}
		return this;
	} 
	
	public SqlBuilder<T> where(String where){
		this.where=where;
		return this;
	}
	
	public SqlBuilder<T> group(String group){
		this.group=group.trim();
		return this;
	}
	
	public SqlBuilder<T> having(String having){
		this.having=having.trim();
		return this;
	}
	
	public SqlBuilder<T> page(long page){
		this.page=page;
		return this;
	} 
	
	public SqlBuilder<T> pageSize(long pageSize){
		this.pageSize=pageSize;
		return this;
	} 
	
	public SqlBuilder<T> needCount(boolean needCount){
		this.needCount=needCount;
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
	
	
	public static class Sort implements Serializable{
		private static final long serialVersionUID = -762743521517102143L;
		
		@Getter
		private String name;
		@Getter
		private String order="DESC";
		
		private Sort(String name,String order) {
			this.setName(name);
			this.setOrder(order);
		}
		
		public static Sort build(String name) {
			return new Sort(name,null);
		}
		public static Sort build(String name,String order) {
			return new Sort(name,order);
		}
		
		/**
		 * 	构建排序信息
		 * @param sorts	eg: age desc,name
		 * @return
		 */
		public static Sort[] builds(String sorts){
			String tt[]=sorts.split(",");
			List<Sort> list=Arrays.asList(tt).stream().map(s->{
				String t[]=s.replaceAll("  ", " ").split(" ");
				if(t.length==1) {
					return new Sort(t[0], null);
				}else if(t.length==2) {
					return new Sort(t[0], t[1]);
				}
				return null;
			}).filter(r->r!=null).collect(Collectors.toList());
			return list.isEmpty()?null:list.toArray(new Sort[list.size()]);
		}
		
		private void setName(String name) {
			if(name!=null && name.matches("[a-z\\_A-Z]*$")) {
				if(name.matches("[A-Z\\_]*$")) {
					this.name=name;
				}else {
					this.name=name.replaceAll("[A-Z]", "_$0").toUpperCase();
				}
			}
		}
		
		private void setOrder(String order) {
			if(order!=null && order.matches("^(?i)(desc|asc)$")) {
				this.order=order.toUpperCase();
			}
		}

		@Override
		public String toString() {
			return String.format("%s %s",this.name, this.order);
		}
		
		public static String use(Sort[] sorts) {
			StringBuffer buf=new StringBuffer();
			for(int i=0;i<sorts.length;i++) {
				buf.append(sorts[i]);
				if(i<(sorts.length-1)) {
					buf.append(",");
				}
			}
			return buf.toString();
		}
		
	}
	
//	public static void main(String[] args) {
//		SqlBuilder<UserPrincipal> builder=SqlBuilder.build(new UserPrincipal())
//				.field("id,name,sex,age")
//				.where("name =? and age> ? and realname like ? and time > #{timeBegin} and time<= #{timeEnd}");
//		System.out.println(builder.countSql());
//	}
	
	
}
