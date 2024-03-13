package com.unione.cloud.beetsql.builder;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.clazz.ClassDesc;
import org.beetl.sql.clazz.TableDesc;
import org.beetl.sql.clazz.kit.BeanKit;
import org.beetl.sql.core.SQLManager;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.annotation.UniDataPermis;
import com.unione.cloud.beetsql.annotation.UniDataPermis.DataPermis;
import com.unione.cloud.beetsql.annotation.UniQueryAction;
import com.unione.cloud.beetsql.annotation.UniQueryIgnore;
import com.unione.cloud.beetsql.annotation.UniQueryKeyWord;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.DataBaseException;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;


/**
 * 	SQL构建对象
 * @author Jeking Yang
 */
@Service
public class SqlBuilder<T> {

	private SQLManager sqlManager;
	private String nameSpace;
	private String key;
	
	private SqlEntity entity=new SqlEntity();
	private String sql;
	private String where;			// 数据过滤条件定义
	private String[] fieldList;		// 数据查询/更新字段
	private String pkField;			// 主键字段名称
	/**
	 * 	更新操作的数据对象
	 */
	@Getter
	private T data;
	/**
	 * 	查询，更新，删除的过滤条件
	 */
	@Getter
	private T params;
	/**
	 * 	查询关键字
	 */
	private String keywords;
	
	private String tableName;	// 数据表名称
	private DataPermis dataPermis;
	
	
	@Getter
	private boolean needCount=true;
	
	@Getter
	private long pageSize = 10;
	@Getter
	private long page = 1;
	
	
	private Pattern fieldRegix=Pattern.compile("[\\w]+");
	private Pattern varRegix=Pattern.compile("\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?[\\s]*\\]");
	private Pattern funRegix=Pattern.compile("[\\s]+(AND|OR)[\\s]+",Pattern.CASE_INSENSITIVE);
	private Pattern inRegix=Pattern.compile("(^IN$)|(^NOT IN$)",Pattern.CASE_INSENSITIVE);
	private Pattern conditionRegix=Pattern.compile("[\\s]*(AND|OR)?[\\s]*[\\w]+[\\s]*(=|>|>=|<|<=|!=|LIKE|(NOT LIKE)|IN|(NOT IN))[\\s]*(\\?|\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?[\\s]*\\])",Pattern.CASE_INSENSITIVE);
	
	
	public SqlBuilder() {}
	
	private SqlBuilder(T params) {
		this.params=params;
		this.data=params;
	}
	
	private SqlBuilder(String tableName, T params) {
		this.params=params;
		this.data=params;
		this.tableName=tableName;
	}
	
	private SqlBuilder(T data,T params) {
		this.params=params;
		this.data=data;
	}
	
	private SqlBuilder(String tableName,T data,T params) {
		this.params=params;
		this.data=data;
		this.tableName=tableName;
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
	public static <T> SqlBuilder<T> build(String tableName,T data,T params) {
		return new SqlBuilder(tableName,data,params);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(Params<T> params) {
		SqlBuilder<T> buildr=new SqlBuilder(params.getBody());
		buildr.page(params.getPage()).pageSize(params.getPageSize());
		return buildr;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(Class<T> cls,Set<Object> ids) {
		try {
			T obj=cls.newInstance();
			BeanUtil.setFieldValue(obj,"ids", new ArrayList(ids));
			SqlBuilder<T> buildr=new SqlBuilder(obj);
			return buildr;
		} catch (Exception e) {
			throw new DataBaseException("构建SqlBuilder实例失败",e);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(Class<T> cls,Object id) {
		try {
			if(id instanceof Set) {
				return build(cls,(Set<Object>)id);
			}
			T obj=BeanKit.newInstance(cls);
			SqlBuilder<T> buildr=new SqlBuilder(obj);
			buildr.setId(id);
			return buildr;
		} catch (Exception e) {
			throw new DataBaseException("构建SqlBuilder实例失败",e);
		}
	}
	
	
	public SqlEntity resolve(SQLManager sqlManager) {
		this.sqlManager=sqlManager;
		AssertUtil.service().notNull(this.tableName, "table name不能为空");
		this.entity.setTable(tableName);
		
		TableDesc tableDesc = this.sqlManager.getTableDesc(this.tableName);
		ClassDesc classDesc=null;
		if(!(this.data instanceof Map)) {
			// 如果是model sql操作
			classDesc = tableDesc.genClassDesc(this.data.getClass(), sqlManager.getNc());
			List<String> idCols = classDesc.getIdCols();
			Iterator<String> cols = classDesc.getInCols().iterator();
			Iterator<String> props = classDesc.getAttrs().iterator();
			while (cols.hasNext() && props.hasNext()) {
				SqlField field=new SqlField();
				field.setAlias(props.next());
				field.setColumn(cols.next());
				if(idCols.contains(field.getColumn())) {
					field.setPk(true);
					this.pkField=field.getColumn();
				}
				PropertyDescriptor pd = BeanUtil.getPropertyDescriptor(this.data.getClass(), field.getAlias());
				field.setType(pd.getPropertyType().getSimpleName());
				this.entity.getFields().add(field);
			}
			
		}else {
			// 如果是动态sql，从字段列表中解析字段信息
			if(this.fieldList!=null && this.fieldList.length>0) {
				String[] list=this.fieldList;
				if(fieldList.length==1 && fieldList[0].indexOf(",")>0) {
					list=fieldList[0].split(",");
				}
				for(int i=0;i<list.length;i++) {
					SqlField field=new SqlField();
					String column=list[i].trim(); 
					Integer asIndex = column.toUpperCase().indexOf(" AS ");
					if(asIndex > 0) {
						String alias = column.substring(asIndex+4);
						column=column.substring(0, asIndex);
						field.setAlias(alias);
					}
					field.setColumn(column);
					if(ObjectUtil.equal(this.pkField, column)) {
						field.setPk(true);
					}
					this.entity.getFields().add(field);
				}
			}
		}
		
		// 自定义sql条件处理
		if(!StringUtils.isEmpty(this.where)) {
			this.processCondition();
		}else if(classDesc!=null){
			// Model 数据库操作，解析通用过滤条件，包括：关键字查询，id查询，常规查询
			
			// 关键字查询
			SqlCondition keyWordCondition=new SqlCondition();
			// id 查询
			SqlCondition idsCondition=new SqlCondition();
			// 常规查询
			List<SqlCondition> normalConditions=new ArrayList<>();
			
			// 迭代字段集合
			this.entity.getFields().stream().forEach(field->{
				
				// 主键查询解析
				if(field.isPk()) {
					SqlCondition ideq=new SqlCondition();
					ideq.setFun(SqlFun.OR);
					ideq.setColumn(field.getColumn());
					ideq.setName(field.getAlias());
					ideq.setAction(SqlAction.EQ);
					SqlCondition idin=new SqlCondition();
					idin.setFun(SqlFun.OR);
					idin.setColumn(field.getColumn());
					idin.setName("ids");
					idin.setAction(SqlAction.IN);
					idsCondition.getChildrens().add(ideq);
					idsCondition.getChildrens().add(idin);
					return;
				}
				
				// 如果是机构编码，则使用右模糊查询
				if(field.getColumn().equals(BaseField.ORGAN_CODE.column())) {
					SqlCondition orgCode=new SqlCondition();
					orgCode.setFun(SqlFun.AND);
					orgCode.setColumn(field.getColumn());
					orgCode.setName(field.getAlias());
					orgCode.setAction(SqlAction.LIKER);
					normalConditions.add(orgCode);
					return;
				}
				
				UniQueryIgnore ignorQuery=BeanKit.getAnnotation(this.data.getClass(), field.getAlias(),UniQueryIgnore.class);
				if(ignorQuery!=null) {
					// 该字段已设置Ignore，忽略
					return;
				}
				
				// 关键字搜索解析
				UniQueryKeyWord keywordQuery=BeanKit.getAnnotation(this.data.getClass(), field.getAlias(),UniQueryKeyWord.class);
				if(keywordQuery!=null) {
					SqlCondition keyword=new SqlCondition();
					keyword.setFun(SqlFun.OR);
					keyword.setColumn(field.getColumn());
					keyword.setName(field.getAlias());
					keyword.setAction(SqlAction.LIKE);
					keyWordCondition.getChildrens().add(keyword);
				}
				
				// 常规搜索解析
				UniQueryAction actionQuery=BeanKit.getAnnotation(this.data.getClass(), field.getAlias(),UniQueryAction.class);
				SqlAction action=SqlAction.EQ;
				if(actionQuery!=null) {
					action=actionQuery.value();
				}
				SqlCondition normal=new SqlCondition();
				normal.setFun(SqlFun.AND);
				normal.setColumn(field.getColumn());
				normal.setName(field.getAlias());
				normal.setAction(action);
				normalConditions.add(normal);
			});
			
			this.entity.getConditions().add(keyWordCondition);
			this.entity.getConditions().add(idsCondition);
			this.entity.getConditions().addAll(normalConditions);
		}
		
		return entity;
	}
	
	
	
	public Class<?> targetClass(){
		return this.data.getClass();
	}
	
	public String nameSpace() {
		if(!StringUtils.isEmpty(this.nameSpace)) {
			return this.nameSpace;
		}
		StackTraceElement stes[]=ThreadUtil.getStackTrace();
		StackTraceElement ste=stes[5];
		String clasName[]=ste.getClassName().split("\\.");
		
		this.nameSpace=String.format("SqlBuilder.%s",clasName[clasName.length-1]);
		return this.nameSpace;
	}
	
	public String key(SqlType type) {
		if(!StringUtils.isEmpty(this.key)) {
			return this.key;
		}
		StackTraceElement stes[]=ThreadUtil.getStackTrace();
		StackTraceElement ste=stes[5];
		this.key=String.format("%s.%s.%s",ste.getMethodName(),type,ste.getLineNumber());
		return this.key;
	}
	
	
	public String toSql(SqlType type) {
		if(SqlType.INSERT.equals(type)) {
			throw new DataBaseException("SqlBuilder暂不支持insert操作");
		}else if(SqlType.UPDATE.equals(type)) {
			return this.updateSql(type);
		}else if(SqlType.UPDATE_BYID.equals(type)) {
			return this.updateSql(type);
		}else if(SqlType.COUNT.equals(type)) {
			return this.countSql();
		}else if(SqlType.SELECT.equals(type)) {
			return this.findSql();
		}else if(SqlType.DELETE.equals(type)){
			return this.deleteSql(type);
		}else if(SqlType.DELETE_BYID.equals(type)){
			return this.deleteSql(type);
		}
		return null;
	}
	
	public Map<String, Object> toParams(){
		Map<String, Object> params=new HashMap<String, Object>();
		Map<String, String> fields=new HashMap<>();
		this.entity.getFields().stream().forEach(field->{
			fields.put(field.getAlias(), field.getColumn());
		});
		params.put("data", this.data);
		params.put("params", this.params);
		params.put("keywords", this.keywords);
		params.put("fields", fields);
		
		DataPermis dataPermis=this.loadDataPermis();
		if(dataPermis!=null && !dataPermis.equals(DataPermis.ALL)) {
			SessionService sessionService=SessionHolder.build();
			switch (dataPermis) {
			case TENANTID:
				BeanUtils.setDefaultValue(this.params, BaseField.TENANT_ID.name(), sessionService.getTenantId());
				break;
			case ORGANID:
				BeanUtils.setDefaultValue(this.params, BaseField.ORGAN_ID.name(), sessionService.getOrgId());
				break;	
			case ORGANCODE:
				BeanUtils.setDefaultValue(this.params, BaseField.ORGAN_CODE.name(), sessionService.getOrgLvsn());
				break;		
			default:
				BeanUtils.setDefaultValue(this.params, BaseField.USER_ID.name(), sessionService.getUserId());
				break;
			}
		}
		return params;
	}
	
	private String countSql() {
		
		
		return null;
	}
	
	private String findSql() {
		return null;
	}
	
	private String updateSql(SqlType type) {
//		if(type.equals(SqlType.UPDATE_BYID)) {
//			this.processIdQuerys();
//		}
//		this.processNormalQuery();
//		this.processCondition();
//		
//		if(StringUtils.isEmpty(this.updateSql)) {
//			List<String> idCols = classDesc.getIdCols();
//			Iterator<String> cols = classDesc.getInCols().iterator();
//			Iterator<String> properties = classDesc.getAttrs().iterator();
//			
//			ConcatContext concatContext = ConcatContext
//					.createTemplateContext(sqlManager.getNc(),new DefaultKeyWordHandler(), sqlManager.getSqlTemplateEngine());
//			
//			Update update = concatContext.update().from(this.data.getClass());
//			
//			while (cols.hasNext() && properties.hasNext()) {
//				String col = cols.next();
//				String prop = properties.next();
//				if(!this.fields.isEmpty() && !this.fields.containsKey(prop)) {
//					continue;
//				}
//				if (classDesc.getClassAnnotation().isUpdateIgnore(prop)) {
//					continue;
//				}
//				if (idCols.contains(col)) {
//					continue;
//				}
//				if (prop.equals(classDesc.getClassAnnotation().getVersionProperty())) {
//					//版本字段
//					update.assignVersion(col);
//					continue;
//				}
//				update.notEmptyAssign(String.format("data.%s", prop), col);
//			}
//			if(StringUtils.isEmpty(this.whereSql)) {
//				// 如果未设置过滤条件，则自动使用主键
//				StringBuffer buf=new StringBuffer();
//				String keyField=classDesc.getIdAttr();
//				buf.append(String.format("%s=?", keyField));
//				this.where=buf.toString();
//				this.processCondition();
//			}
//			
//			this.updateSql=String.format("%s %s", update.toSql(),this.whereSql);
//		}
//		return this.updateSql;
		return null;
	}
	
	
	private String deleteSql(SqlType type) {
		return null;
	}
	
//	private void processIdQuerys() {
//		if(StringUtils.isEmpty(this.where)) {
//			// 如果未设置过滤条件，则自动使用主键
//			StringBuffer buf=new StringBuffer();
//			String keyField=classDesc.getIdAttr();
//			buf.append(String.format("(%s=? OR %s in [ids])", keyField,keyField));
//			
//			this.where=buf.toString();
//		}
//	}
//	
//	private void processKeywordsQuery() {
//		// keywords条件处理
//		if(StringUtils.isEmpty(this.keywordsQuery)) {
//			try {
//				StringBuffer buf=new StringBuffer();
//				PropertyDescriptor ps[] = BeanKit.propertyDescriptors(this.targetClass());
//				for(PropertyDescriptor p:ps) {
//					UniQueryKeyWord keyWordQuery=BeanKit.getAnnotation(this.targetClass(), p.getName(),UniQueryKeyWord.class);
//					if(keyWordQuery!=null) {
//						buf.append(" OR ").append(p.getName()).append(" LIKE [%keywords%]");
//					}
//				}
//				if(buf.length()>0) {
//					this.keywordsQuery=buf.substring(4, buf.length());
//				}
//			} catch (IntrospectionException e) {
//			}
//		}	
//		if(!StringUtils.isEmpty(this.keywordsQuery)) {
//			if(StringUtils.isEmpty(this.where)) {
//				this.where=this.keywordsQuery;
//			}else {
//				this.where=String.format("%s \n-- @SQLTRIM_{AND (%s) \n--@}\n",this.where, this.keywordsQuery);
//			}
//		}
//	}
//	
//	private void processLikeQuery() {
//		// like查询条件处理
//		if(StringUtils.isEmpty(this.likesQuery)) {
//			try {
//				StringBuffer buf=new StringBuffer();
//				PropertyDescriptor ps[] = BeanKit.propertyDescriptors(this.targetClass());
//				for(PropertyDescriptor p:ps) {
//					UniQueryLike likeQuery=BeanKit.getAnnotation(this.targetClass(), p.getName(),UniQueryLike.class);
//					if(likeQuery!=null) {
//						buf.append(" AND ").append(p.getName());
//						switch (likeQuery.value()) {
//						case LEFT:
//							buf.append(" LIKE [%").append(p.getName()).append("]");
//							break;
//						case RIGHT:
//							buf.append(" LIKE [").append(p.getName()).append("%]");
//							break;
//						default:
//							buf.append(" LIKE [%").append(p.getName()).append("%]");
//							break;
//						}
//					}
//				}
//				if(buf.length()>0) {
//					this.likesQuery=buf.toString();
//				}
//			} catch (IntrospectionException e) {
//			}
//		}	
//		if(!StringUtils.isEmpty(this.likesQuery)) {
//			if(StringUtils.isEmpty(this.where)) {
//				this.where=this.likesQuery;
//			}else {
//				this.where=String.format("%s %s",this.where, this.likesQuery);
//			}
//		}
//	}
//	
//	private void processNormalQuery() {
//		if(StringUtils.isEmpty(this.normalQuery)) {
//			List<String> idCols = classDesc.getIdCols();
//			Iterator<String> cols = classDesc.getInCols().iterator();
//			Iterator<String> properties = classDesc.getAttrs().iterator();
//			StringBuffer buffer=new StringBuffer();
//			
//			while (cols.hasNext() && properties.hasNext()) {
//				String col = cols.next();
//				String prop = properties.next();
//				if (idCols.contains(col)) {
//					// 主键字段，忽略
//					continue;
//				}
//				UniQueryLike likeQuery=BeanKit.getAnnotation(this.targetClass(), prop,UniQueryLike.class);
//				if(likeQuery!=null) {
//					// 该字段已设置like查询，忽略
//					continue;
//				}
//				UniQueryIgnore ignorQuery=BeanKit.getAnnotation(this.targetClass(), prop,UniQueryIgnore.class);
//				if(ignorQuery!=null) {
//					// 该字段已设置Ignore，忽略
//					continue;
//				}
//				
//				// 如果是机构编码，则使用右模糊查询
//				if(col.equals(BaseField.ORGAN_CODE.column())) {
//					buffer.append("AND ").append(col).append(" LIKE ").append("[").append(prop).append("%] ");
//					continue;
//				}
//				
//				UniQueryAction actionQuery=BeanKit.getAnnotation(this.targetClass(), prop,UniQueryAction.class);
//				ACTION action=ACTION.EQ;
//				if(actionQuery!=null) {
//					action=actionQuery.value();
//				}
//				buffer.append("AND ").append(col).append(action.express()).append("[").append(prop).append("] ");
//			}
//			if(buffer.length()>0) {
//				this.normalQuery=buffer.substring(4);
//			}
//		}
//		if(!StringUtils.isEmpty(this.normalQuery)) {
//			if(StringUtils.isEmpty(this.where)) {
//				this.where=this.normalQuery;
//			}else {
//				this.where=String.format("%s AND %s",this.where, this.normalQuery);
//			}
//		}
//	}
	
	/**
	 * Sql Where 处理
	 */
	private void processCondition() {
		if(!StringUtils.isEmpty(this.sql) || StringUtils.isEmpty(this.where)) {
			return;
		}
		
		// where条件处理
		Matcher matcher=conditionRegix.matcher(this.where);
		String whereSql=this.where.replaceAll("\\(", "\r\n-- @SQLTRIM_{\r\n(")
				.replaceAll("\\)", ")\r\n-- @}\r\n")
				.replaceAll("@SQLTRIM_", "@sqlTrim()");
		while(matcher.find()) {
			String condition=matcher.group();
			whereSql=whereSql.replace(condition, whereCondition(condition));
		}
		DataPermis dataPermis=this.loadDataPermis();
		if(dataPermis!=null && !dataPermis.equals(DataPermis.ALL)) {
			switch (dataPermis) {
			case TENANTID:
				whereSql=String.format("%s AND %s = #{params.%s}", whereSql,BaseField.TENANT_ID.column(),BaseField.TENANT_ID.name());
				break;
			case ORGANID:
				whereSql=String.format("%s AND %s = #{params.%s}", whereSql,BaseField.ORGAN_ID.column(),BaseField.ORGAN_ID.name());
				break;	
			case ORGANCODE:
				whereSql=String.format("%s AND %s LIKE #{params.%s+'%'}", whereSql,BaseField.ORGAN_CODE.column(),BaseField.ORGAN_CODE.name());
				break;		
			default:
				whereSql=String.format("%s AND %s = #{params.%s}", whereSql,BaseField.USER_ID.column(),BaseField.USER_ID.name());
				break;
			}
		}
		this.entity.setWhere(String.format("\r\n-- @sqlWhere(){\r\n%s\r\n-- @}\r\n", whereSql));
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
		if(!fieldName.matches("^[A-Z\\_]*$")) {
			condition=condition.replaceFirst(fieldName, fieldName.replaceAll("[A-Z]", "_$0").toUpperCase());
		}
		
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
	
	private DataPermis loadDataPermis() {
		if(this.dataPermis==null && !(this.data instanceof Map)) {
			UniDataPermis dataPermis = this.data.getClass().getAnnotation(UniDataPermis.class);
			if(dataPermis!=null) {
				this.dataPermis=dataPermis.value();
			}
		}
		return this.dataPermis;
	}
	
	/**
	 * 	设置数据权限级别
	 * @param dataPermis
	 * @return
	 */
	public SqlBuilder<T> dataPermis(DataPermis dataPermis){
		this.dataPermis=dataPermis;
		return this;
	}
	
	public SqlBuilder<T> field(String... fieldList){
		this.fieldList=fieldList;
		return this;
	} 
	
	public SqlBuilder<T> keywords(String keywords){
		this.keywords=keywords;
		return this;
	}
	
	public SqlBuilder<T> query(String sql){
		this.sql=sql;
		return this;
	}
	
	public SqlBuilder<T> where(String where){
		this.where=where;
		return this;
	}
	
	public SqlBuilder<T> pkField(String pkField){
		this.pkField=pkField;
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
	
	public Object getId() {
		SqlField pkField=this.entity.getPkField();
		AssertUtil.database().notNull(pkField, "主键字段不能为空");
		return BeanUtil.getFieldValue(this.params, pkField.getAlias());
	}
	public SqlBuilder<T> setId(Object id) {
		SqlField pkField=this.entity.getPkField();
		AssertUtil.database().notNull(pkField, "主键字段不能为空");
		BeanUtil.setFieldValue(this.params, pkField.getAlias(),id);
		return this;
	}
	
	public Object getTenantId() {
		return BeanUtil.getFieldValue(this.params, BaseField.TENANT_ID.name());
	}
	public Object getOrgId() {
		return BeanUtil.getFieldValue(this.params, BaseField.ORGAN_ID.name());
	}
	public Object getUserId() {
		return BeanUtil.getFieldValue(this.params, BaseField.USER_ID.name());
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
		INSERT,UPDATE,UPDATE_BYID,SELECT,COUNT,DELETE,DELETE_BYID
	}
	
//	public static void main(String[] args) {
//		SqlBuilder<UserPrincipal> builder=SqlBuilder.build(new UserPrincipal())
//				.field("id,name,sex,age")
//				.where("name =? and age> ? and realname like ? and time > #{timeBegin} and time<= #{timeEnd}");
//		System.out.println(builder.countSql());
//	}
	
	
}
