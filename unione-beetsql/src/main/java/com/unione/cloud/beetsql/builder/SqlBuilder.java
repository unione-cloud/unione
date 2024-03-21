package com.unione.cloud.beetsql.builder;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.clazz.ClassDesc;
import org.beetl.sql.clazz.TableDesc;
import org.beetl.sql.clazz.kit.BeanKit;
import org.beetl.sql.core.SQLManager;

import com.unione.cloud.beetsql.annotation.UniDataPermis;
import com.unione.cloud.beetsql.annotation.UniDataPermis.DataPermis;
import com.unione.cloud.beetsql.annotation.UniDataSensitive;
import com.unione.cloud.beetsql.annotation.UniQueryAction;
import com.unione.cloud.beetsql.annotation.UniQueryIgnore;
import com.unione.cloud.beetsql.annotation.UniQueryKeyWord;
import com.unione.cloud.beetsql.utils.SensitiveUtil;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.DataBaseException;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.model.BaseField.StsField;
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
public class SqlBuilder<T> {

	private SQLManager sqlManager;
	private String nameSpace;
	
	private SqlEntity entity=new SqlEntity();
	private String where;			// 数据过滤条件定义
	private String[] fieldList;		// 数据查询/更新字段
	private String keyField;		// 主键字段名称
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
	
	private String tableName;	// 数据表名称
	private DataPermis dataPermis;
	
	/**
	 * 	查询关键字
	 */
	private String keywords;
	
	private Long id;
	private List<Long> ids;
	
	private Sort sort[];
	
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
	
	private boolean initComplete;
	
	private SqlBuilder() {}
	
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
		buildr.page(params.getPage())
			  .pageSize(params.getPageSize())
			  .needCount(params.isNeedCount())
			  .keywords(params.getKeywords());
		if(!StringUtils.isEmpty(params.getSortName())) {
			String stns[]=params.getSortName().split(",");
			String sons[]=null;
			if(!StringUtils.isEmpty(params.getSortOrder())) {
				sons=params.getSortOrder().split(",");
			}
			Sort sort[]=new Sort[stns.length];
			for(int i=0;i<stns.length;i++) {
				String so=(sons!=null&&(i<sons.length)?sons[i]:"asc");
				sort[i]=Sort.build(stns[i], so);
			}
			buildr.sort(sort);
		}
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
	public static <T> SqlBuilder<T> build(Class<T> cls,Long id) {
		try {
			T obj=BeanKit.newInstance(cls);
			SqlBuilder<T> buildr=new SqlBuilder(obj);
			buildr.id(id);
			return buildr;
		} catch (Exception e) {
			throw new DataBaseException("构建SqlBuilder实例失败",e);
		}
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(Class<T> cls,List<Long> ids) {
		try {
			T obj=BeanKit.newInstance(cls);
			SqlBuilder<T> buildr=new SqlBuilder(obj);
			buildr.ids(ids);
			return buildr;
		} catch (Exception e) {
			throw new DataBaseException("构建SqlBuilder实例失败",e);
		}
	}
	
	
	public void init(SQLManager sqlManager) {
		this.sqlManager=sqlManager;
		if(StringUtils.isEmpty(this.tableName)) {
			this.tableName=sqlManager.getNc().getTableName(this.data.getClass());
		}
		AssertUtil.service().notNull(this.tableName, "table name不能为空");
		this.entity.setTable(tableName);
		
		SqlField pkField=this.entity.getKeyField();
		if(pkField==null) {
			TableDesc tableDesc = this.sqlManager.getTableDesc(this.tableName);
			String idn = tableDesc.getIdNames().iterator().next();
			pkField=new SqlField();
			pkField.setAlias(idn);
			this.entity.setKeyField(pkField);
		}
		
		if(!this.initComplete) {
			this.initComplete=true;
			this.resolve();
		}
	}
	
	private void resolve() {
		TableDesc tableDesc = this.sqlManager.getTableDesc(this.tableName);
		ClassDesc classDesc=null;
		if(!(this.data instanceof Map)) {
			
			List<String> fieldLists=new ArrayList<>();
			if(this.fieldList!=null && this.fieldList.length>0) {
				String[] list=this.fieldList;
				if(fieldList.length==1 && fieldList[0].indexOf(",")>0) {
					list=fieldList[0].split(",");
				}
				for(int i=0;i<list.length;i++) {
					String column=list[i].trim(); 
					Integer asIndex = column.toUpperCase().indexOf(" AS ");
					if(asIndex > 0) {
						String alias = column.substring(asIndex+4);
						column=column.substring(0, asIndex);
						fieldLists.add(alias);
						fieldLists.add(column);
					}else {
						fieldLists.add(column);
					}
				}
			}
			
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
					this.keyField=field.getColumn();
				}else if(StringUtils.isEmpty(this.keyField)) {
					if(ObjectUtil.equal(this.keyField, field.getColumn()) || 
							ObjectUtil.equal(this.keyField, field.getAlias())) {
						field.setPk(true);
					}
				}
				
				// 判断是否为基础字段
				StsField stsField=BaseField.isBaseColume(field.getColumn());
				field.setStsField(stsField);
				
				PropertyDescriptor pd = BeanUtil.getPropertyDescriptor(this.data.getClass(), field.getAlias());
				field.setType(pd.getPropertyType().getSimpleName());
				
				// 如果设置了数据脱敏
				UniDataSensitive dataSensitive=BeanKit.getAnnotation(this.data.getClass(), field.getAlias(),UniDataSensitive.class);
				if(dataSensitive!=null && "String".contentEquals(field.getType())) {
					field.setSensitive(SqlSensitive.build(dataSensitive));
				}
				
				if(!fieldLists.isEmpty()) {
					if(field.isPk() || fieldLists.contains(field.getAlias()) || fieldLists.contains(field.getColumn())) {
						this.entity.getFields().add(field);
					}
				}else {
					this.entity.getFields().add(field);
				}
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
					if(ObjectUtil.equal(this.keyField, field.getColumn()) || 
							ObjectUtil.equal(this.keyField, field.getAlias())) {
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
					ideq.setAction(SqlAction.ID);
					SqlCondition idin=new SqlCondition();
					idin.setFun(SqlFun.OR);
					idin.setColumn(field.getColumn());
					idin.setName("ids");
					idin.setAction(SqlAction.IDS);
					idsCondition.getChildrens().add(ideq);
					idsCondition.getChildrens().add(idin);
					return;
				}
				
				// 如果是机构编码，则使用右模糊查询
				if(field.getColumn().equals(BaseField.ORGAN_CODE.getColumn())) {
					SqlCondition orgCode=new SqlCondition();
					orgCode.setFun(SqlFun.AND);
					orgCode.setColumn(field.getColumn());
					orgCode.setName(field.getAlias());
					orgCode.setAction(SqlAction.LIKER);
					normalConditions.add(orgCode);
					return;
				}

				// 如果是区域编码，则使用右模糊查询
				if(field.getColumn().equals(BaseField.AREA_CODE.getColumn())) {
					SqlCondition areaCode=new SqlCondition();
					areaCode.setFun(SqlFun.AND);
					areaCode.setColumn(field.getColumn());
					areaCode.setName(field.getAlias());
					areaCode.setAction(SqlAction.LIKER);
					normalConditions.add(areaCode);
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
					keyword.setAction(SqlAction.KEYWORD);
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
			
			if(!keyWordCondition.getChildrens().isEmpty()) {
				this.entity.getConditions().add(keyWordCondition);
			}
			if(!idsCondition.getChildrens().isEmpty()) {
				this.entity.getConditions().add(idsCondition);
			}
			if(!normalConditions.isEmpty()) {
				this.entity.getConditions().addAll(normalConditions);
			}
		}
		
	}
	
	public String toSql(SqlType type) {
		
		if(!StringUtils.isEmpty(this.entity.getSql())) {
			return this.entity.getSql();
		}
		boolean isJavaBean=!(this.data instanceof Map);
		
		StringBuffer buffer=new StringBuffer();
		if(SqlType.SELECT.equals(type)) {
			buffer.append("SELECT ");
			
			// 查询字段处理
			StringBuffer fieldBuf=new StringBuffer();
			this.entity.getFields().stream().forEach(field->{
				fieldBuf.append(",").append(field.getColumn());
				if(!isJavaBean && !StringUtils.isEmpty(field.getAlias())) {
					fieldBuf.append(" AS ").append(field.getAlias());
				}
			});
			if(fieldBuf.length()==0) {
				buffer.append("* FROM ");
			}else {
				buffer.append(fieldBuf.substring(1)).append(" FROM ");
			}
			
			// 查询表名称处理
			if(!StringUtils.isEmpty(this.entity.getSchema())) {
				buffer.append(this.entity.getSchema()).append(".");
			}
			buffer.append(this.entity.getTable()).append(" ");
		}else if(SqlType.COUNT.equals(type)) {
			buffer.append("SELECT COUNT(*) FROM ");
			if(!StringUtils.isEmpty(this.entity.getSchema())) {
				buffer.append(this.entity.getSchema()).append(".");
			}
			buffer.append(this.entity.getTable()).append(" ");
		}else if("UPDATE".equalsIgnoreCase(type.value())) {
			buffer.append("UPDATE ");
			if(!StringUtils.isEmpty(this.entity.getSchema())) {
				buffer.append(this.entity.getSchema()).append(".");
			}
			buffer.append(this.entity.getTable()).append(" SET \n")
			.append("-- @sqlTrim(){\n");
			
			this.entity.getFields().stream().forEach(field->{
				if(field.getStsField()!=null && (BaseField.LAST_UPDATED.getName().equals(field.getStsField().getName()) || 
						BaseField.LAST_UPDATED_BY.getName().equals(field.getStsField().getName()))) {
					return;
				}
				buffer.append("-- @if(isNotEmpty(fields.").append(field.getAlias()).append(")){\n")
				      .append(field.getColumn()).append(" = #{params.").append(field.getAlias()).append("},\n")
				      .append("-- @}\n");
			});
			
			StringBuffer lastUpBuf=new StringBuffer();
			this.entity.getBaseFields().stream().forEach(field->{
				if(BaseField.LAST_UPDATED.getName().equals(field.getStsField().getName()) || 
						BaseField.LAST_UPDATED_BY.getName().equals(field.getStsField().getName())) {
					lastUpBuf.append(",").append(field.getColumn()).append(" = #{params.").append(field.getAlias()).append("}\n");
				}
			});
			if(lastUpBuf.length()>0) {
				buffer.append(lastUpBuf.substring(1));
			}
			
			buffer.append("-- @}\n");
		}else if("DELETE".equalsIgnoreCase(type.value())) {
			buffer.append("DELETE FROM ");
			if(!StringUtils.isEmpty(this.entity.getSchema())) {
				buffer.append(this.entity.getSchema()).append(".");
			}
			buffer.append(this.entity.getTable()).append(" ");
		}
		
		// where 条件处理
		if(!StringUtils.isEmpty(this.entity.getWhere())) {
			buffer.append(this.entity.getWhere());
		}else if(!this.entity.getConditions().isEmpty()){
			buffer.append("\n-- @sqlWhere(){\n");
			this.entity.getConditions().stream().forEach(con->{
				con.toSql(buffer);
			});
			buffer.append("-- @}\n");
		}
		
		// sort 排序处理
		if(SqlType.SELECT.equals(type)) {
			buffer.append("-- @if(!isEmpty(sorts)){\n")
			      .append("ORDER BY #{text(sorts)}\n")
			      .append("-- @}");
		}
		
		return buffer.toString();
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
	
	public String sqlId(SqlType type) {
		StackTraceElement stes[]=ThreadUtil.getStackTrace();
		StackTraceElement ste=stes[5];
		return String.format("%s.%s.%s",ste.getMethodName(),type,ste.getLineNumber());
	}
	
	public Map<String, Object> toParams(){
		Map<String, Object> params=new HashMap<String, Object>();
		
		Map<String, String> fields=new HashMap<>();
		this.entity.getFields().stream().forEach(field->{
			fields.put(field.getAlias(), field.getColumn());
			
			// 数据脱敏处理
			if(field.getSensitive()!=null) {
				Object value=BeanUtil.getFieldValue(this.data, field.getAlias());
				if(value!=null && value instanceof String) {
					String sensitiveValue=SensitiveUtil.process(value.toString(), field.getSensitive());
					BeanUtil.setFieldValue(this.data, field.getAlias(), sensitiveValue);
				}
			}
		});
		
		
		params.put("data", this.data);
		params.put("params", this.params);
		params.put("fields", fields);
		if(this.sort!=null && this.sort.length>0) {
			params.put("sorts", Sort.use(this.sort));
		}
		
		Map<String, Object> query=new HashMap<>();
		query.put("keywords", this.keywords);
		query.put("id", this.id);
		if(id==null) {
			String keyField=this.keyField;
			if(StringUtils.isEmpty(keyField)) {
				SqlField pkField=this.entity.getKeyField();
				if(pkField!=null) {
					keyField=pkField.getAlias();
				}
			}
			query.put("id", BeanUtil.getFieldValue(this.params, keyField));
		}
		query.put("ids", this.ids);
		params.put("query", query);
		
		// 数据权限处理
		DataPermis dataPermis=this.loadDataPermis();
		if(dataPermis!=null && !dataPermis.equals(DataPermis.ALL)) {
			SessionService sessionService=SessionHolder.build();
			switch (dataPermis) {
			case TENANTID:
				BeanUtils.setDefaultValue(this.params, BaseField.TENANT_ID.getName(), sessionService.getTenantId());
				break;
			case ORGANID:
				BeanUtils.setDefaultValue(this.params, BaseField.ORGAN_ID.getName(), sessionService.getOrgId());
				break;	
			case ORGANCODE:
				BeanUtils.setDefaultValue(this.params, BaseField.ORGAN_CODE.getName(), sessionService.getOrgLvsn());
				break;		
			case AREACODE:
				BeanUtils.setDefaultValue(this.params, BaseField.AREA_CODE.getName(), sessionService.getAreaCode());
				break;		
			default:
				BeanUtils.setDefaultValue(this.params, BaseField.USER_ID.getName(), sessionService.getUserId());
				break;
			}
		}
		
		return params;
	}
	
	
	/**
	 * Sql Where 处理
	 */
	private void processCondition() {
		if(!StringUtils.isEmpty(this.entity.getSql()) || StringUtils.isEmpty(this.where)) {
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
				whereSql=String.format("(%s) AND %s = #{params.%s}", whereSql,BaseField.TENANT_ID.getColumn(),BaseField.TENANT_ID.getName());
				break;
			case ORGANID:
				whereSql=String.format("(%s) AND %s = #{params.%s}", whereSql,BaseField.ORGAN_ID.getColumn(),BaseField.ORGAN_ID.getName());
				break;	
			case ORGANCODE:
				whereSql=String.format("(%s) AND %s LIKE #{params.%s+'%'}", whereSql,BaseField.ORGAN_CODE.getColumn(),BaseField.ORGAN_CODE.getName());
				break;	
			case AREACODE:
				whereSql=String.format("(%s) AND %s LIKE #{params.%s+'%'}", whereSql,BaseField.AREA_CODE.getColumn(),BaseField.AREA_CODE.getName());
				break;	
			default:
				whereSql=String.format("(%s) AND %s = #{params.%s}", whereSql,BaseField.USER_ID.getColumn(),BaseField.USER_ID.getName());
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
	
	public SqlBuilder<T> id(Long id){
		this.id=id;
		return this;
	}
	
	public SqlBuilder<T> ids(List<Long> ids){
		this.ids=ids;
		return this;
	}
	
	public SqlBuilder<T> query(String sql){
		this.entity.setSql(sql);
		return this;
	}
	
	public SqlBuilder<T> where(String where){
		this.where=where;
		return this;
	}
	
	public SqlBuilder<T> sort(Sort ...sort){
		this.sort=sort;
		return this;
	}
	
	public SqlBuilder<T> key(String keyField){
		this.keyField=keyField;
		return this;
	}
	
	public SqlBuilder<T> needCount(boolean needCount){
		this.needCount=needCount;
		return this;
	} 
	
//	public SqlBuilder<T> setId(Object id) {
//		SqlField pkField=this.entity.getPkField();
//		AssertUtil.database().notNull(pkField, "主键字段不能为空");
//		BeanUtil.setFieldValue(this.params, pkField.getAlias(),id);
//		return this;
//	}
	
	public SqlBuilder<T> page(long page){
		this.page=page;
		return this;
	} 
	
	public SqlBuilder<T> pageSize(long pageSize){
		this.pageSize=pageSize;
		return this;
	} 
	
	public long getStart() {
		return (page - 1) * pageSize;
	}
	
	
	
}
