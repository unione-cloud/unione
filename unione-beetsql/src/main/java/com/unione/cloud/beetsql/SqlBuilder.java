package com.unione.cloud.beetsql;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.clazz.ClassDesc;
import org.beetl.sql.clazz.TableDesc;
import org.beetl.sql.clazz.kit.BeanKit;
import org.beetl.sql.clazz.kit.DefaultKeyWordHandler;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.concat.ConcatContext;
import org.beetl.sql.core.concat.Update;
import org.beetl.sql.core.engine.SQLParameter;
import org.beetl.sql.mapper.annotation.SqlResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.annotation.KeyWordQuery;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.model.Pojo;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * 	SQL构建对象
 * @author Jeking Yang
 */
@Slf4j
@Service
public class SqlBuilder<T> {

	/**
	 * 查询名称，指定查询SQL名称
	 */
	@Getter
	private String name;
	
	private String key;
	
	private String[] fieldList;				// 数据查询/更新字段
	@Getter
	private Map<String, String> fields=new HashMap<>(); 	// 字段map集合
	@Getter
	private T data;				// 更新操作的数据对象
	@Getter
	private T params;			// 查询，更新，删除的过滤条件
	
	private String nameSpace;
	private String tableName;	// 数据表名称
	private TableDesc tableDesc;
	private ClassDesc classDesc;
	
	private String keywords;	// 关键字搜索字段
	private String where;		// 数据过滤条件定义
	private Sort[] sorts;		// 数据排序
	private String group;		// 数据分组
	private String having;		// 分组处理
	
	@Getter
	private boolean needCount=true;
	
	private String countSql;
	private String findSql;
	private String whereSql;
	private String updateSql;
	private String deleteSql;
	
	@Getter
	private List<Object> values=new ArrayList<Object>();
	
	@Getter
	private long pageSize = 10;
	@Getter
	private long page = 1;
	
	
	private Pattern fieldRegix=Pattern.compile("[\\w]+");
	private Pattern varRegix=Pattern.compile("\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?[\\s]*\\]");
	private Pattern funRegix=Pattern.compile("[\\s]+(AND|OR)[\\s]+",Pattern.CASE_INSENSITIVE);
	private Pattern inRegix=Pattern.compile("IN|(NOT IN)",Pattern.CASE_INSENSITIVE);
	private Pattern conditionRegix=Pattern.compile("[\\s]*(AND|OR)?[\\s]*[\\w]+[\\s]*(=|>|>=|<|<=|!=|LIKE|(NOT LIKE)|IN|(NOT IN))[\\s]*(\\?|\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?[\\s]*\\])",Pattern.CASE_INSENSITIVE);
	
	
	@Getter
	private List<SQLParameter> conditions=new ArrayList<>();
	
	
	private static SQLManager sqlManager;
	@Autowired
	public void setSqlManager(SQLManager sqlManager) {
		SqlBuilder.sqlManager=sqlManager;
	}
	
	public SqlBuilder() {}
	
	private SqlBuilder(T params) {
		this.params=params;
		this.data=params;
		
		this.tableName=sqlManager.getNc().getTableName(this.data.getClass());
		this.tableDesc = sqlManager.getTableDesc(this.tableName);
		this.classDesc = this.tableDesc.genClassDesc(this.data.getClass(), sqlManager.getNc());
	}
	
	private SqlBuilder(String tableName, T params) {
		this.params=params;
		this.data=params;
		
		this.tableName=tableName;
		this.tableDesc = sqlManager.getTableDesc(this.tableName);
		this.classDesc = this.tableDesc.genClassDesc(this.data.getClass(), sqlManager.getNc());
	}
	
	private SqlBuilder(T data,T params) {
		this.params=params;
		this.data=data;
		
		this.tableName=sqlManager.getNc().getTableName(this.data.getClass());
		this.tableDesc = sqlManager.getTableDesc(this.tableName);
		this.classDesc = this.tableDesc.genClassDesc(this.data.getClass(), sqlManager.getNc());
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
	public static <T> SqlBuilder<T> build(String tableName,T params) {
		return new SqlBuilder(tableName,params);
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
	
	public String nameSpace() {
		if(StringUtils.isEmpty(this.nameSpace)) {
			SqlResource sqlResource = this.data.getClass().getAnnotation(SqlResource.class);
			if(sqlResource!=null) {
				this.nameSpace=sqlResource.value();
				return this.nameSpace;
			}
			
			if(StringUtils.isEmpty(this.tableName)) {
				this.nameSpace=this.data.getClass().getSimpleName();
			}else {
				String attr[] = this.tableName.toLowerCase().split("_");
				this.nameSpace = "";
				for(int i = 0; i < attr.length; i++){
					String temp = attr[i];
					String tt = (temp.charAt(0)+"").toUpperCase();
					if(temp.length() > 1){
						tt += temp.substring(1, temp.length());
					}
					this.nameSpace += tt;
				}
			}
			this.nameSpace=(this.nameSpace.charAt(0)+"").toLowerCase()+this.nameSpace.substring(1);
		}
		return this.nameSpace;
	}
	
	
	public String toSql(SqlType type) {
		if(SqlType.INSERT.equals(type)) {
			
		}else if(SqlType.UPDATE.equals(type)) {
			return this.updateSql();
		}else if(SqlType.COUNT.equals(type)) {
			return this.countSql();
		}else if(SqlType.SELECT.equals(type)) {
			return this.findSql();
		}else if(SqlType.DELETE.equals(type)){
			return this.deleteSql();
		}
		return null;
	}
	
	public Map<String, Object> toParams(){
		Map<String, Object> params=new HashMap<String, Object>();
		params.put("data", this.data);
		params.put("params", this.params);
		params.put("fields", this.fields);
		return params;
	}
	
	private String countSql() {
		this.processIdQuerys();
		this.processKeywordsQuery();
		this.processCondition();
		
		if(StringUtils.isEmpty(this.countSql)) {
			this.countSql=String.format("SELECT COUNT(*) FROM %s %s ",this.tableName,this.whereSql);
		}
		return this.countSql;
	}
	
	private String findSql() {
		if(StringUtils.isEmpty(this.findSql)) {
			this.processIdQuerys();
			this.processKeywordsQuery();
			this.processCondition();
			
			String selectField="*";
			if(this.fieldList!=null && this.fieldList.length>0) {
				selectField=ArrayUtil.join(this.fieldList, ",");
			}
			this.findSql=String.format("SELECT %s FROM %s %s ",selectField,this.tableName,this.whereSql);
		}
		return this.findSql;
	}
	
	private String updateSql() {
		this.processCondition();
		if(StringUtils.isEmpty(this.updateSql)) {
//			AssertUtil.database().isTrue(!fields.isEmpty(), "更新字段不能为空");
			List<String> idCols = classDesc.getIdCols();
			Iterator<String> cols = classDesc.getInCols().iterator();
			Iterator<String> properties = classDesc.getAttrs().iterator();
			
			ConcatContext concatContext = ConcatContext
					.createTemplateContext(sqlManager.getNc(),new DefaultKeyWordHandler(), sqlManager.getSqlTemplateEngine());
			
			Update update = concatContext.update().from(this.data.getClass());
			while (cols.hasNext() && properties.hasNext()) {
				String col = cols.next();
				String prop = properties.next();
				if (classDesc.getClassAnnotation().isUpdateIgnore(prop)) {
					continue;
				}
				if (idCols.contains(col)) {
					continue;
				}
				if (prop.equals(classDesc.getClassAnnotation().getVersionProperty())) {
					//版本字段
					update.assignVersion(col);
					continue;
				}
				update.notEmptyAssign(String.format("data.%s", prop), col);
			}
			if(StringUtils.isEmpty(this.whereSql)) {
				// 如果未设置过滤条件，则自动使用主键
				StringBuffer buf=new StringBuffer();
				String keyField=classDesc.getIdAttr();
				buf.append(String.format("%s=?", keyField));
				this.where=buf.toString();
				this.processCondition();
			}
			this.updateSql=String.format("%s %s", update.toSql(),this.whereSql);
		}
		return this.updateSql;
	}
	
	
	private String deleteSql() {
		this.processIdQuerys();
		this.processCondition();
		if(StringUtils.isEmpty(this.deleteSql)) {
			this.deleteSql=String.format("DELETE FROM %s %s",this.tableName,this.whereSql);
		}
		return this.deleteSql;
	}
	
	private void processIdQuerys() {
		if(StringUtils.isEmpty(this.where)) {
			// 如果未设置过滤条件，则自动使用主键
			StringBuffer buf=new StringBuffer();
			String keyField=classDesc.getIdAttr();
			buf.append(String.format("(%s=? OR %s in [ids])", keyField,keyField));
			
			this.where=buf.toString();
		}
	}
	
	private void processKeywordsQuery() {
		// keywords条件处理
		if(StringUtils.isEmpty(this.keywords)) {
			try {
				StringBuffer buf=new StringBuffer();
				PropertyDescriptor ps[] = BeanKit.propertyDescriptors(this.targetClass());
				for(PropertyDescriptor p:ps) {
					KeyWordQuery keyWordQuery=BeanKit.getAnnotation(this.targetClass(), p.getName(),KeyWordQuery.class);
					if(keyWordQuery!=null) {
						buf.append(" OR ").append(p.getName()).append(" LIKE [%keywords%]");
					}
				}
				if(buf.length()>0) {
					this.keywords=String.format("(AND %s)", buf.substring(4, buf.length()));
				}
			} catch (IntrospectionException e) {
			}
		}	
		if(!StringUtils.isEmpty(this.keywords)) {
			if(StringUtils.isEmpty(this.where)) {
				this.where=this.keywords;
			}else {
				this.where=String.format("%s %s",this.where, this.keywords);
			}
		}
	}
	
	/**
	 * Sql Where 处理
	 */
	private void processCondition() {
		if(!StringUtils.isEmpty(this.whereSql)) {
			return;
		}
		
		if(StringUtils.isEmpty(this.where)) {
			this.whereSql="";
			return;
		}
		
		// where条件处理
		Matcher matcher=conditionRegix.matcher(this.where);
		String where=this.where.replaceAll("\\(", "\r\n-- @ SQLTRIM_{\r\n(")
				.replaceAll("\\)", ")\r\n-- @}\r\n")
				.replaceAll("@ SQLTRIM_", "@ sqlTrim()");
		this.whereSql="\r\n-- @ sqlWhere(){\r\n"+where+"\r\n-- @}\r\n";
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
		condition=condition.replaceFirst(fieldName, fieldName.replaceAll("[A-Z]", "_$0").toUpperCase());
		
		Matcher varMatcher=varRegix.matcher(condition);
		if(varMatcher.find()) {
			// [复杂变量处理]，eg：[%name%]
			String group=varMatcher.group();
			String paramName=group.substring(1, group.length()-1);
			
			// 变量名称处理
			if(paramName.indexOf("?")>=0) {
				paramName=paramName.replace("?", String.format("params.%s", fieldName));
			}else {
				fieldName=paramName.trim();
				if(fieldName.startsWith("%")) {
					paramName="%"+String.format("params.%s", fieldName.substring(1));
				}else {
					paramName=String.format("params.%s", fieldName);
				}
			}
			
			// 模糊查询处理
			if(paramName.indexOf("%")>=0) {
				fieldName=fieldName.replaceAll("%", "");
				paramName=paramName.replaceAll("%","+'%'+").trim();
				if(paramName.startsWith("+")) {
					paramName=paramName.substring(1);
				}
				if(paramName.endsWith("+")) {
					paramName=paramName.substring(0, paramName.length()-1);
				}
			}
			
			// IN ， NOT IN 查询处理
			Matcher inMatcher = inRegix.matcher(condition);
			if(inMatcher.find()) {
				paramName=String.format("join(%s)", paramName);
				condition=condition.replace(group, String.format("(#{%s})", paramName));
			}else {
				condition=condition.replace(group, String.format("#{%s}", paramName));
			}
			
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
	
	public SqlBuilder<T> keywords(String keywords){
		this.keywords=keywords;
		return this;
	}
	
	public SqlBuilder<T> query(String sql){
		this.findSql=sql;
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
	
	public String getKey() {
		if(!StringUtils.isEmpty(this.key)) {
			return MD5.create().digestHex(this.key);
		}
		StackTraceElement stes[] = ThreadUtil.getStackTrace();
		StringBuffer buffer=new StringBuffer();
		for(StackTraceElement ste:stes) {
			String clasName[]=ste.getClassName().split("\\.");
			String tmp=String.format("%s.%s", clasName[clasName.length-1],ste.getMethodName());
			buffer.append(tmp).append("->");
		}
		this.key=buffer.substring(0, buffer.length()-2);
		return MD5.create().digestHex(this.key);
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
		INSERT,UPDATE,SELECT,COUNT,DELETE
	}
	
//	public static void main(String[] args) {
//		SqlBuilder<UserPrincipal> builder=SqlBuilder.build(new UserPrincipal())
//				.field("id,name,sex,age")
//				.where("name =? and age> ? and realname like ? and time > #{timeBegin} and time<= #{timeEnd}");
//		System.out.println(builder.countSql());
//	}
	
	
}
