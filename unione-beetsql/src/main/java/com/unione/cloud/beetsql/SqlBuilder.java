package com.unione.cloud.beetsql;

import java.beans.PropertyDescriptor;
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
import org.beetl.sql.core.engine.SQLParameter;

import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Pojo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * 	SQL构建对象
 * @author Jeking Yang
 */
@Slf4j
public class SqlBuilder<T> {

	@Getter
	private String name;
	
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
	
	private Pattern fieldRegix=Pattern.compile("[\\w]+");
	private Pattern varRegix=Pattern.compile("\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?[\\s]*\\]");
	private Pattern funRegix=Pattern.compile("[\\s]*(AND|OR)[\\s]*",Pattern.CASE_INSENSITIVE);
	private Pattern conditionRegix=Pattern.compile("[\\s]*(AND|OR)?[\\s]*[\\w]+[\\s]*(=|>|>=|<|<=|LIKE|IN|(NOT IN))[\\s]*(\\?|\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?[\\s]*\\])",Pattern.CASE_INSENSITIVE);
	
	
	@Getter
	private List<SQLParameter> conditions=new ArrayList<>();
	
	private SqlBuilder(T params) {
		this.params=params;
		this.data=params;
		Table table = params.getClass().getAnnotation(Table.class);
		if(table!=null) {
			this.tableName=table.name();
		}
		this.name=String.format("sql.builder.%s", params.getClass().getName());
	}
	
	private SqlBuilder(T data,T params) {
		this.params=params;
		this.data=data;
		Table table = params.getClass().getAnnotation(Table.class);
		if(table!=null) {
			this.tableName=table.name();
		}
		
		this.name=String.format("sql.builder.%s", data.getClass().getName());
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
	
	public String toSql(SqlType type) {
		if(SqlType.INSERT.equals(type)) {
			
		}else if(SqlType.UPDATE.equals(type)) {
			return this.updateSql();
		}else if(SqlType.COUNT.equals(type)) {
			return this.countSql();
		}else if(SqlType.FIND.equals(type)) {
			return this.findSql();
		}else if(SqlType.DELETE.equals(type)){
			
		}
		return null;
	}
	
	private String countSql() {
		this.process();
		
		if(this.countSql==null) {
			this.countSql=String.format("SELECT COUNT(*) FROM %s %s ",this.tableName,this.whereSql);
		}
		return this.countSql;
	}
	
	private String findSql() {
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
	
	private String updateSql() {
		
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
			return;
		}
		
		// (name=? and age>? and realname like [?+'%'] and title like [keyword+'%']) 
		// and types in ? and subtype not in [subtypes]
		// and (time>[timeBegin] or time<=[timeEnd])
		log.info("SQL 查询条件处理 where:{}",where);
		
		// 思路： 正则替换成beetl函数处理，
		Matcher matcher=conditionRegix.matcher(this.where);
		this.where=this.where.replaceAll("\\(", "\r\n-- @ SQLTRIM_{\r\n(")
				.replaceAll("\\)", ")\r\n-- @}\r\n")
				.replaceAll("@ SQLTRIM_", "@ sqlTrim()");
		this.whereSql="\r\n-- @ where(){\r\n"+this.where+"\r\n-- @}\r\n";
		while(matcher.find()) {
			String condition=matcher.group();
			this.whereSql=this.whereSql.replace(condition, whereCondition(condition));
		}
		
	}
	
	private String whereCondition(String condition) {
		Matcher funMatcher=funRegix.matcher(condition);
		String funName="";
		if(funMatcher.find()) {
			funName=funMatcher.group();
			condition=condition.replace(funName, "");
		}
		
		Matcher fieldMatcher=fieldRegix.matcher(condition);
		fieldMatcher.find();
		String fieldName=fieldMatcher.group();
		
		// 字段名称变成大写
		condition=condition.replace(fieldName, fieldName.replaceAll("[A-Z]", "_$0").toUpperCase());
		
		Matcher varMatcher=varRegix.matcher(condition);
		if(varMatcher.find()) {
			// [复杂变量处理]，eg：[%name%]
			String group=varMatcher.group();
			String paramName=group.substring(1, group.length()-1);
			
			// 变量名称处理
			if(paramName.indexOf("?")>=0) {
				paramName=paramName.replace("?", String.format("params.%s", fieldName));
			}else {
				paramName=paramName.replaceAll("(?<=\\s|^)(?=\\S)", "params.");
			}
			
			// 模糊查询处理
			if(paramName.indexOf("%")>=0) {
				paramName=paramName.replaceAll("%","+'%'+").trim();
				if(paramName.startsWith("+")) {
					paramName=paramName.substring(1);
				}
				if(paramName.endsWith("+")) {
					paramName=paramName.substring(0, paramName.length()-1);
				}
			}
			condition=condition.replace(group, String.format("#{%s}", paramName));
		}else {
			condition=condition.replace("?", String.format("#{params.%s}", fieldName));
		}
		return String.format("\n-- @if(varNotNull(params.%s)){\n%s%s\n-- @}\n",fieldName,funName,condition);
	}
	
	public SqlBuilder<T> field(String... fieldList){
		String[] list=fieldList;
		if(fieldList.length==1 && fieldList[0].indexOf(",")>0) {
			list=fieldList[0].split(",");
		}
		this.fields=new HashMap<>();
		this.fieldList=new String[list.length];
		for(int i=0;i<list.length;i++) {
			String field=list[i].trim(); 
			String colName=field.replaceAll("[A-Z]", "_$0").toUpperCase();
			Integer asIndex = field.toUpperCase().indexOf(" AS ");
			if(asIndex > 0) {
				colName=field.substring(0, asIndex).replaceAll("[A-Z]", "_$0").toUpperCase()+field.substring(asIndex);
			}
			this.fields.put(field,colName);
			this.fieldList[i]=colName;
		}
		return this;
	} 
	
	public SqlBuilder<T> name(String name){
		this.name=name;
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
	
	public static enum SqlType{
		INSERT,UPDATE,FIND,COUNT,DELETE
	}
	
//	public static void main(String[] args) {
//		SqlBuilder<UserPrincipal> builder=SqlBuilder.build(new UserPrincipal())
//				.field("id,name,sex,age")
//				.where("name =? and age> ? and realname like ? and time > #{timeBegin} and time<= #{timeEnd}");
//		System.out.println(builder.countSql());
//	}
	
	
}
