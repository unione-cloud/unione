package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.HashMap;
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

import com.unione.cloud.beetsql.Sort;
import com.unione.cloud.beetsql.annotation.DataPermis;
import com.unione.cloud.beetsql.annotation.DataPermis.PermisRule;
import com.unione.cloud.beetsql.annotation.KeyWords;
import com.unione.cloud.beetsql.annotation.QueryAction;
import com.unione.cloud.beetsql.annotation.QueryIgnore;
import com.unione.cloud.beetsql.annotation.QueryIgnore.QueryType;
import com.unione.cloud.beetsql.utils.SensitiveUtil;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.DataBaseException;
import com.unione.cloud.core.model.BaseField;
import com.unione.cloud.core.security.SessionHolder;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.security.UserRoles;
import com.unione.cloud.core.util.BeanUtils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.MD5;
import lombok.Getter;


/**
 * 	SQL构建对象
 * @author Jeking Yang
 */
public class SqlBuilder<T> {

	private SQLManager sqlManager;
	private String nameSpace;
	
	@Getter
	private SqlEntity entity=new SqlEntity();

	private List<LinkEntity> linkEntitys=new ArrayList<>();
	/**
	 * 自定义主键字段，例如：id
	 */
	private String keyField;
	/**
	 * 自定义查询条件，例如：where 1=1 and id=#{id}
	 */
	private String where;

	/**
	 * 	更新操作的数据对象
	 */
	@Getter
	private T data;
	/**
	 * 	查询，更新，删除的过滤条件
	 */
	@Getter
	private Object params;
	
	private String tableName;	// 数据表名称
	private PermisRule dataPermis;
	
	/**
	 * 	查询关键字
	 */
	private String keywords;
	
	private Long id;
	private List<Long> ids;
	
	private Sort sort[];
	
	@Getter
	private boolean needCount=true;

	private boolean queryIgnore=true;
	
	@Getter
	private int pageSize = 10;
	@Getter
	private int page = 1;
	
	
	private Pattern fieldRegix=Pattern.compile("[\\w]+");
	private Pattern varRegix=Pattern.compile("\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?\\.?\\w*[\\s]*\\]");
	private Pattern funRegix=Pattern.compile("[\\s]+(AND|OR)[\\s]+",Pattern.CASE_INSENSITIVE);
	private Pattern inRegix=Pattern.compile("( IN )|( NOT IN )",Pattern.CASE_INSENSITIVE);
	private Pattern conditionRegix=Pattern.compile("[\\s]*(AND|OR)?[\\s]*[\\w]+[\\s]*(=|>|>=|<|<=|!=|LIKE|(NOT LIKE)|IN|(NOT IN)|IS)[\\s]*(\\?|(NULL)|(NOT NULL)|\\[[\\s]*%?[\\s]*\\w*\\??[\\s]*%?\\.?\\w*[\\s]*\\])",Pattern.CASE_INSENSITIVE);
	private Pattern humpFieldRegix=Pattern.compile("[\\s]*([a-z]+[0-9]*[A-Z]+[\\w]*|[a-z0-9]+)[\\s]*(=|>|>=|<|<=|!=|LIKE|(NOT LIKE)|IN|(NOT IN)|IS|like|(not like)|in|(not in)|is)[\\s]*");
	
	private boolean initComplete;
	
	private SqlBuilder() {}
	
	private SqlBuilder(T params) {
		this.params=params;
		this.data=params;
	}
	
	private SqlBuilder(String tableName, T params) {
		this.params=params;
		this.data=params;
		tableName=(""+tableName.charAt(0)).toLowerCase()+tableName.substring(1);
		this.tableName=tableName.replaceAll("[A-Z]", "_$0").toUpperCase();
	}
	
	private SqlBuilder(T data,Object params) {
		this.params=params;
		this.data=data;
	}
	
	private SqlBuilder(String tableName,T data,Object params) {
		this.params=params;
		this.data=data;
		tableName=(""+tableName.charAt(0)).toLowerCase()+tableName.substring(1);
		this.tableName=tableName.replaceAll("[A-Z]", "_$0").toUpperCase();
	}
	
	/**
	 * 	构建Finder实例
	 * @param params
	 * @return
	 */
	@SuppressWarnings({ "unchecked"})
	public static <T> SqlBuilder<T> build(T params) {
		return new SqlBuilder(params);
	}

	@SuppressWarnings({ "unchecked"})
	public static SqlBuilder<Map<String,Object>> build(String tableName) {
		return new SqlBuilder(tableName,new HashMap<>());
	}
	
	@SuppressWarnings({ "unchecked"})
	public static <T> SqlBuilder<T> build(String tableName,T params) {
		return new SqlBuilder(tableName,params);
	}
	
	@SuppressWarnings({ "unchecked"})
	public static <T> SqlBuilder<T> build(String tableName,T data,Object params) {
		return new SqlBuilder(tableName,data,params);
	}
	
	@SuppressWarnings({ "unchecked"})
	public static <T> SqlBuilder<T> build(Params<T> params) {
		SqlBuilder<T> buildr=new SqlBuilder(params.getBody());
		buildr.page(params.getPage())
			  .pageSize(params.getPageSize())
			  .needCount(params.isNeedCount())
			  .keywords(params.getKeywords())
			  .ids(params.getIds());
		if(!ObjectUtil.isEmpty(params.getSorts())) {
			List<Sort> sorts = params.getSorts().stream()
				.filter(s->s!=null)
				.map(s->Sort.build(s.getName(), s.isAsc()?"ASC":"DESC"))
				.collect(Collectors.toList());
			if(!sorts.isEmpty()) {
				buildr.sort(sorts.toArray(new Sort[] {}));
			}
		}
		return buildr;
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(Class<T> cls) {
		try {
			T obj=BeanKit.newInstance(cls);
			SqlBuilder<T> buildr=new SqlBuilder(obj,new HashMap<>());
			return buildr;
		} catch (Exception e) {
			throw new DataBaseException("构建SqlBuilder实例失败",e);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> SqlBuilder<T> build(Class<T> cls,Object params) {
		try {
			T obj=BeanKit.newInstance(cls);
			if(params instanceof Set){
				SqlBuilder<T> buildr=new SqlBuilder(obj,new HashMap<>());
				buildr.ids=new ArrayList<>((Set)params);
				return buildr;
			}
			if(params instanceof List){
				SqlBuilder<T> buildr=new SqlBuilder(obj,new HashMap<>());
				buildr.ids((List)params);
				return buildr;
			}
			if(params instanceof Long){
				SqlBuilder<T> buildr=new SqlBuilder(obj,new HashMap<>());
				buildr.id((Long)params);
				return buildr;
			}
			if(params instanceof Params){
				Params<Object> params2=(Params)params;
				SqlBuilder<T> buildr=new SqlBuilder(obj,params2.getBody());
				buildr.page(params2.getPage())
					.pageSize(params2.getPageSize())
					.needCount(params2.isNeedCount())
					.keywords(params2.getKeywords());
				if(!ObjectUtil.isEmpty(params2.getSorts())) {
					List<Sort> sorts = params2.getSorts().stream()
						.filter(s->s!=null)
						.map(s->Sort.build(s.getName(), s.isAsc()?"ASC":"DESC"))
						.collect(Collectors.toList());
					if(!sorts.isEmpty()) {
						buildr.sort(sorts.toArray(new Sort[] {}));
					}
				}
				return buildr;
			}
			return new SqlBuilder(obj,params);
		} catch (Exception e) {
			throw new DataBaseException("构建SqlBuilder实例失败",e);
		}
	}
	
	/**
	 * 初始化
	 * @param sqlManager
	 */
	public void init(SQLManager sqlManager) {
		this.sqlManager=sqlManager;
		if(StringUtils.isEmpty(this.tableName)) {
			this.tableName=sqlManager.getNc().getTableName(this.data.getClass());
		}
		AssertUtil.service().notNull(this.tableName, "table name不能为空");
		this.entity.setTable(tableName);
		
		if(!this.initComplete) {
			this.initComplete=true;
			this.resolve();
		}
	}
	

	/**
	 * 解析查询信息
	 */
	@SuppressWarnings("unchecked")
	private void resolve() {
		TableDesc tableDesc = this.sqlManager.getTableDesc(this.tableName);
		ClassDesc classDesc=null;
		if(!(this.data instanceof Map)) {
			// 如果是model sql操作
			classDesc = tableDesc.genClassDesc(this.data.getClass(), sqlManager.getNc());
			List<SqlField> fields=SqlKit.loadFields(sqlManager, this.data.getClass(), tableName);
			this.entity.setFields(fields);
		}else {
			// 如果是动态sql，从字段列表中解析字段信息
			List<String> fieldList=this.entity.getFieldList();
			if(!ObjectUtil.isEmpty(fieldList)) {
				for(int i=0;i<fieldList.size();i++) {
					SqlField field=new SqlField();
					String column=fieldList.get(i).trim(); 
					Integer asIndex = column.toUpperCase().indexOf(" AS ");
					if(asIndex > 0) {
						String alias = column.substring(asIndex+4);
						column=column.substring(0, asIndex);
						field.setAlias(alias);
					}
					field.setColumn(column);

					// 判断是否为主键字段
					if(ObjectUtil.equal(this.keyField, field.getColumn()) || 
							ObjectUtil.equal(this.keyField, field.getAlias())) {
						field.setPk(true);
						this.entity.setKeyField(field);
					}

					this.entity.getFields().add(field);
				}
			}else{
				Map<String,Object> tmp=(Map<String,Object>)this.data;
				tableDesc.getCols().stream().forEach(col->{
					SqlField field=new SqlField();
					String column=col.trim(); 
					Integer asIndex = column.toUpperCase().indexOf(" AS ");
					if(asIndex > 0) {
						String alias = column.substring(asIndex+4);
						column=column.substring(0, asIndex);
						field.setAlias(alias);
					}else{
						field.setAlias(StrUtil.toCamelCase(column));
					}
					field.setColumn(column);

					// 判断是否为主键字段
					if(ObjectUtil.equal(this.keyField, field.getColumn()) || 
							ObjectUtil.equal(this.keyField, field.getAlias())) {
						field.setPk(true);
						this.entity.setKeyField(field);
					}
					
					// 如果data中有改字段value，则加入fields集合
					if(tmp.get(field.getAlias())!=null){
						this.entity.getFields().add(field);
					}
				});
			}
		}
		
		// 自定义sql条件处理
		if(!StringUtils.isEmpty(this.where)) {
			this.processWhereCondition();
		}else if(classDesc!=null){
			// Model 数据库操作，解析通用过滤条件，包括：关键字查询，id查询，常规查询
			// 关键字查询
			SqlCondition keyWordCondition=new SqlCondition();
			keyWordCondition.setAction(SqlAction.KEYWORD);
			
			// 常规查询
			List<SqlCondition> normalConditions=new ArrayList<>();
			
			// 迭代字段集合
			this.entity.getFields().stream().forEach(field->{
				
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
				
				
				QueryIgnore ignorQuery=BeanKit.getAnnotation(this.data.getClass(), field.getAlias(),QueryIgnore.class);
				if(ignorQuery!=null) {
					// 该字段已设置Ignore，忽略
					return;
				}
				
				// 关键字搜索解析
				KeyWords keywordQuery=BeanKit.getAnnotation(this.data.getClass(), field.getAlias(),KeyWords.class);
				if(keywordQuery!=null) {
					SqlCondition keyword=new SqlCondition();
					keyword.setFun(SqlFun.OR);
					keyword.setColumn(field.getColumn());
					keyword.setName(field.getAlias());
					keyWordCondition.getChildrens().add(keyword);
				}
				
				// 常规搜索解析
				QueryAction actionQuery=BeanKit.getAnnotation(this.data.getClass(), field.getAlias(),QueryAction.class);
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
			if(!normalConditions.isEmpty()) {
				this.entity.getConditions().addAll(normalConditions);
			}
		}
		
	}
	
	/**
	 * 生成SQL语句
	 * @param type
	 * @return
	 */
	public String toSql(SqlType type) {
		
		if(!StringUtils.isEmpty(this.entity.getSql())) {
			return this.entity.getSql();
		}
		boolean isJavaBean=!(this.data instanceof Map);
		StringBuffer buffer=new StringBuffer();

		if(SqlType.INSERT.equals(type)) {
			buffer.append("INSERT INTO ");
			if(!StringUtils.isEmpty(this.entity.getSchema())) {
				buffer.append(this.entity.getSchema()).append(".");
			}
			buffer.append(this.entity.getTable()).append(" (");
			for(int i=0;i<this.entity.getFields().size();i++) {
				SqlField field=this.entity.getFields().get(i);
				buffer.append(field.getColumn());
				if(i<this.entity.getFields().size()-1) {
					buffer.append(",");
				}
			}
			buffer.append(") VALUES (");
			for(int i=0;i<this.entity.getFields().size();i++) {
				SqlField field=this.entity.getFields().get(i);
				buffer.append("#{data.").append(field.getAlias()).append("}");
				if(i<this.entity.getFields().size()-1) {
					buffer.append(",");
				}
			}	
			buffer.append(")");
			return buffer.toString();
		}
		
		if(SqlType.SELECT.equals(type)||SqlType.SELECT_BYID.equals(type)||SqlType.SELECT_ONE.equals(type)) {
			buffer.append("SELECT ");
			// 查询字段处理
			StringBuffer fieldBuf=new StringBuffer();
			this.entity.getFields().stream()
			.filter(field->ObjectUtil.isEmpty(entity.getFieldList()) || entity.getFieldList().contains(field.getAlias()))
			.filter(field->{
				// 如果字段设置了过滤
				if(field.getQueryIgnore()!=null && queryIgnore==true) {
					if(!ObjectUtil.isEmpty(entity.getFieldList())){
						if(entity.getFieldList().contains(field.getAlias())){
							return true;
						}
					}
					if(QueryType.SELECT.equals(field.getQueryIgnore().value())) {
						return false;
					}
					if(QueryType.SELECT_LIST.equals(field.getQueryIgnore().value()) && SqlType.SELECT.equals(type)) {
						return false;
					}
					if(QueryType.SELECT_ONE.equals(field.getQueryIgnore().value()) && (SqlType.SELECT_ONE.equals(type) || 
							SqlType.SELECT_BYID.equals(type))) {
						return false;
					}
				}
				return true;
			}).forEach(field->{
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
		}else if(SqlType.UPDATE.equals(type) || SqlType.UPDATE_BYID.equals(type) ||
				SqlType.DELETE_LOGIC.equals(type) || SqlType.DELETE_LOGIC_BYID.equals(type)) {
			buffer.append("UPDATE ");
			if(!StringUtils.isEmpty(this.entity.getSchema())) {
				buffer.append(this.entity.getSchema()).append(".");
			}
			buffer.append(this.entity.getTable()).append(" SET \n")
			.append("-- @sqlTrim(){\n");
			
			this.entity.getFields().stream()
				// .filter(field->ObjectUtil.isEmpty(entity.getFieldList()) || entity.getFieldList().contains(field.getAlias()))
				.filter(field->!field.isPk()).forEach(field->{
				if(field.getStsField()!=null && (
						BaseField.ID.getName().equals(field.getStsField().getName()) || 
						// BaseField.TENANT_ID.getName().equals(field.getStsField().getName()) ||
						// BaseField.CREATED.getName().equals(field.getStsField().getName()) ||
						// BaseField.CREATED_BY.getName().equals(field.getStsField().getName()) ||
						BaseField.LAST_UPDATED.getName().equals(field.getStsField().getName()) ||
						BaseField.LAST_UPDATED_BY.getName().equals(field.getStsField().getName()))) {
					return;
				}
				buffer.append("-- @if(isNotEmpty(fields.").append(field.getAlias()).append(")){\n")
				      .append(field.getColumn()).append(" = #{data.").append(field.getAlias()).append("},\n")
				      .append("-- @}\n");
			});
			
			StringBuffer lastUpBuf=new StringBuffer();
			this.entity.getStsFields(BaseField.LAST_UPDATED,BaseField.LAST_UPDATED_BY)
				.stream().forEach(field->{
					lastUpBuf.append(",").append(field.getColumn()).append(" = #{data.").append(field.getAlias()).append("}\n");
				});
			if(lastUpBuf.length()>0) {
				buffer.append(lastUpBuf.substring(1));
			}
			
			buffer.append("-- @}\n");
			
		}else if(SqlType.DELETE.equals(type) || SqlType.DELETE_BYID.equals(type)) {
			buffer.append("DELETE FROM ");
			if(!StringUtils.isEmpty(this.entity.getSchema())) {
				buffer.append(this.entity.getSchema()).append(".");
			}
			buffer.append(this.entity.getTable()).append(" ");
		}
		
		// 如果是findById,updateById,deleteById并且未设置查询条件，生成更新条件
		if(StringUtils.isEmpty(this.entity.getWhere()) && 
				(SqlType.UPDATE_BYID.equals(type) || SqlType.DELETE_BYID.equals(type) || SqlType.DELETE_LOGIC_BYID.equals(type) ||
						SqlType.SELECT_BYID.equals(type))) {
			SqlField idField=this.entity.getKeyField();
			AssertUtil.service().notNull(idField,"主键字段不能为空");
			StringBuffer where=new StringBuffer();
			where.append("\n-- @sqlWhere(){\n");
			
			where.append("-- @if(notNull(params.").append(idField.getAlias()).append(") || notNull(query.id) || notNull(query.ids)){\n");
			where.append("-- @if(notNull(params.").append(idField.getAlias()).append(") && !notNull(query.id) && !notNull(query.ids)){\n")
		      .append(idField.getColumn()).append(" = #{params.").append(idField.getAlias()).append("}\n")
		      .append("-- @}\n");
			where.append("-- @if(notNull(query.id) && !notNull(query.ids)){\n")
		      .append(idField.getColumn()).append(" = #{query.id}\n")
		      .append("-- @}\n");
			where.append("-- @if(!notNull(params.").append(idField.getAlias()).append(") && !notNull(query.id) && notNull(query.ids)){\n")
		      .append(idField.getColumn()).append(" IN (#{join(query.ids)})\n")
		      .append("-- @}\n");
			
			this.entity.getStsFields(BaseField.TENANT_ID,BaseField.ORGAN_ID,BaseField.USER_ID).stream().forEach(field->{
				where.append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){\n")
			      .append(" AND ").append(field.getColumn()).append(" = #{params.").append(field.getAlias()).append("}\n")
			      .append("-- @}\n");
			});

			if(SqlType.SELECT_BYID.equals(type)){
				this.entity.getStsFields(BaseField.DEL_FLAG).stream().forEach(field->{
					where.append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){\n")
					.append(" AND ").append(field.getColumn()).append(" = #{params.").append(field.getAlias()).append("}\n")
					.append("-- @}\n");
				});
			}

			this.entity.getStsFields(BaseField.ORGAN_CODE).stream().forEach(field->{
				where.append("-- @if(isNotEmpty(params.").append(field.getAlias()).append(")){\n")
			      .append(" AND ").append(field.getColumn()).append(" LIKE #{params.").append(field.getAlias()).append("+'%'}\n")
			      .append("-- @}\n");
			});
			
			where.append("-- @}\n");
			where.append("-- @}\n");
			this.entity.setWhere(where.toString());
		}
		
		// where 条件处理
		if(!StringUtils.isEmpty(this.entity.getWhere())) {
			buffer.append(this.entity.getWhere());
			this.linkEntitys.stream().forEach(link->{
				buffer.append(link.toSql(sqlManager));
			});
		}else if(!this.entity.getConditions().isEmpty()){
			buffer.append("\n-- @sqlWhere(){\n");
			this.entity.getConditions().stream().forEach(con->{
				con.toSql(buffer);
			});
			this.linkEntitys.stream().forEach(link->{
				buffer.append(link.toSql(sqlManager));
			});
			buffer.append("-- @}\n");
		}

		
		// sort 排序处理
		if(SqlType.SELECT.equals(type) || SqlType.SELECT_ONE.equals(type) || SqlType.SELECT_BYID.equals(type)) {
			buffer.append("-- @if(!isEmpty(sorts)){\n")
			      .append("ORDER BY #{text(sorts)}\n")
			      .append("-- @}");
		}
		
		return buffer.toString();
	}
		
	/**
	 * 获取目标实体类
	 * @return
	 */
	public Class<?> targetClass(){
		return this.data.getClass();
	}
		
	/**
	 * 生成命名空间
	 * @return
	 */
	public String nameSpace() {
		if(!StringUtils.isEmpty(this.nameSpace)) {
			return this.nameSpace;
		}
		StackTraceElement stes[]=ThreadUtil.getStackTrace();
		StackTraceElement ste=stes[5];
		String clasName[]=ste.getClassName().split("\\.");

		String fields=this.entity.getFields().stream().map(f->f.getAlias()).collect(Collectors.joining(","));
		this.nameSpace=String.format("SqlBuilder.%s.%s",clasName[clasName.length-1],MD5.create().digestHex(fields));
		return this.nameSpace;
	}
	
	/**
	 * 生成SQL ID
	 * @param type
	 * @return
	 */
	public String sqlId(SqlType type) {
		StackTraceElement stes[]=ThreadUtil.getStackTrace();
		StackTraceElement ste=stes[5];
		return String.format("%s.%s.%s",ste.getMethodName(),type,ste.getLineNumber());
	}
	
	/**
	 * 参数转换
	 * @return
	 */
	public Map<String, Object> toParams(){
		Map<String, Object> params=new HashMap<String, Object>();
		
		Map<String, String> fields=new HashMap<>();
		this.entity.getFields().stream()
			.filter(field->ObjectUtil.isEmpty(entity.getFieldList()) || entity.getFieldList().contains(field.getAlias()))	
			.forEach(field->{
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
		params.put("principal", SessionHolder.build().getPrincipal());
		params.put("query", query);

		// 关联查询条件
		this.linkEntitys.stream().forEach(link->{
			params.put(String.format("%sLinkParams", link.getField()), link.getParams());
		});
		
		// 数据权限处理
		PermisRule dataPermis=this.loadDataPermis();
		SessionService sessionService=SessionHolder.build();
		if(dataPermis!=null && !dataPermis.equals(PermisRule.ALL) && (!sessionService.isAdmin() && 
			!sessionService.getUserRoles().contains(UserRoles.SUPPERADMIN))) {
			switch (dataPermis) {
			case TENANTID:
				BeanUtils.setFieldValue(this.params, BaseField.TENANT_ID.getName(), sessionService.getTenantId());
				break;
			case ORGANID:
				BeanUtils.setFieldValue(this.params, BaseField.ORGAN_ID.getName(), sessionService.getOrgId());
				break;	
			case ORGANCODE:
				BeanUtils.setFieldValue(this.params, BaseField.ORGAN_CODE.getName(), sessionService.getOrgLvsn());
				break;		
			case AREACODE:
				BeanUtils.setFieldValue(this.params, BaseField.AREA_CODE.getName(), sessionService.getAreaCode());
				break;		
			default:
				BeanUtils.setFieldValue(this.params, BaseField.USER_ID.getName(), sessionService.getUserId());
				break;
			}
		}
		
		return params;
	}
	
	
	/**
	 * Sql Where 处理
	 */
	private void processWhereCondition() {
		if(!StringUtils.isEmpty(this.entity.getSql()) || StringUtils.isEmpty(this.where)) {
			return;
		}
		
		// where条件处理
		Matcher matcher=conditionRegix.matcher(this.where);
		String whereSql=this.where.replaceAll("\\(", "\r\n-- @SQLTRIM_{\r\n(")
				.replaceAll("\\)", ")\r\n-- @}\n")
				.replaceAll("@SQLTRIM_", "@sqlTrim()");
		while(matcher.find()) {
			String condition=matcher.group();
			whereSql=whereSql.replace(condition, whereCondition(condition));
		}
		
		// 静态参数字段名称处理
		matcher=humpFieldRegix.matcher(whereSql);
		while(matcher.find()) {
			String condition=matcher.group();
			whereSql=whereSql.replace(condition, condition.replaceAll("[A-Z]", "_$0").toUpperCase());
		}
		
		// 关键字查询处理
		if(!ObjectUtil.isEmpty(this.entity.getFields())){
			// 关键字查询
			SqlCondition keyWordCondition=new SqlCondition();
			keyWordCondition.setAction(SqlAction.KEYWORD);
			this.entity.getFields().stream().forEach(field->{
				// 关键字搜索解析
				KeyWords keywordQuery=BeanKit.getAnnotation(this.data.getClass(), field.getAlias(),KeyWords.class);
				if(keywordQuery!=null) {
					SqlCondition keyword=new SqlCondition();
					keyword.setFun(SqlFun.OR);
					keyword.setColumn(field.getColumn());
					keyword.setName(field.getAlias());
					keyWordCondition.getChildrens().add(keyword);
				}
			});
			if(!ObjectUtil.isEmpty(keyWordCondition.getChildrens())){
				StringBuffer buffer=new StringBuffer(whereSql);
				keyWordCondition.toSql(buffer);
				whereSql=buffer.toString();
			}
		}

		// 数据权限处理
		PermisRule dataPermis=this.loadDataPermis();
		if(dataPermis!=null && !dataPermis.equals(PermisRule.ALL)) {
			switch (dataPermis) {
			case TENANTID:
				whereSql=String.format("(%s)\n-- @if(notNull(params.%s)){\n AND %s = #{params.%s}\n-- @}\n",whereSql,
					BaseField.TENANT_ID.getName(),BaseField.TENANT_ID.getColumn(),BaseField.TENANT_ID.getName());
				break;
			case ORGANID:
				whereSql=String.format("(%s)\n-- @if(notNull(params.%s)){\n AND %s = #{params.%s}\n-- @}\n",whereSql,
					BaseField.ORGAN_ID.getName(),BaseField.ORGAN_ID.getColumn(),BaseField.ORGAN_ID.getName());
				break;	
			case ORGANCODE:
				whereSql=String.format("(%s)\n-- @if(notNull(params.%s)){\n AND %s LIKE #{params.%s+'%%'}\n-- @}\n",
					whereSql,BaseField.ORGAN_CODE.getName(),BaseField.ORGAN_CODE.getColumn(),BaseField.ORGAN_CODE.getName());
				break;	
			case AREACODE:
				whereSql=String.format("(%s)\n-- @if(notNull(params.%s)){\n AND %s LIKE #{params.%s+'%%'}\n-- @}\n",
					whereSql,BaseField.AREA_CODE.getName(),BaseField.AREA_CODE.getColumn(),BaseField.AREA_CODE.getName());
				break;	
			default:
				whereSql=String.format("(%s)\n-- @if(notNull(params.%s)){\n AND %s = #{params.%s}\n-- @}\n",
					whereSql,BaseField.USER_ID.getName(),BaseField.USER_ID.getColumn(),BaseField.USER_ID.getName());
				break;
			}
		}
		this.entity.setWhere(String.format("\r\n-- @sqlWhere(){\r\n%s\r\n-- @}\r\n", whereSql));
	}
	
	/**
	 * 处理Where条件
	 * @param condition
	 * @return
	 */
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
				}else if(!fieldName.startsWith("data.") && !fieldName.startsWith("query.") &&
					!fieldName.startsWith("params.") && !fieldName.startsWith("principal.")){
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
		if(fieldName.startsWith("data.") || fieldName.startsWith("query.") || 
					fieldName.startsWith("params.") || fieldName.startsWith("principal.")){
			return String.format("\n-- @if(notNull(%s)){\n%s%s\n-- @}\n",fieldName,funName,condition);
		}
		return String.format("\n-- @if(notNull(params.%s)){\n%s%s\n-- @}\n",fieldName,funName,condition);
	}
	
	/**
	 * 加载数据权限级别
	 * @return
	 */
	private PermisRule loadDataPermis() {
		if(this.dataPermis==null && !(this.data instanceof Map)) {
			DataPermis dataPermis = this.data.getClass().getAnnotation(DataPermis.class);
			if(dataPermis!=null) {
				this.dataPermis=dataPermis.value();
			}
		}
		if(this.dataPermis!=null) {
			return this.dataPermis;	
		}
		return null;
	}
	
	/**
	 * 	设置数据权限级别
	 * @param dataPermis
	 * @return
	 */
	public SqlBuilder<T> dataPermis(PermisRule dataPermis){
		this.dataPermis=dataPermis;
		return this;
	}
		
	/**
	 * 设置查询/更新字段列表
	 * @param fieldList
	 * @return
	 */
	public SqlBuilder<T> field(String... fieldList){
		if(fieldList!=null){
			for(String field:fieldList){
				if(field!=null){
					String[] list=field.split(",");
					for(int i=0;i<list.length;i++) {
						String column=list[i].trim(); 
						Integer asIndex = column.toUpperCase().indexOf(" AS ");
						if(asIndex > 0) {
							String alias = column.substring(asIndex+4);
							column=column.substring(0, asIndex);
							this.entity.getFieldList().add(alias);
							this.entity.getFieldList().add(column);
						}else {
							this.entity.getFieldList().add(column);
						}
					}
				}
			}
		}
		return this;
	} 
		
	/**
	 * 设置查询关键词
	 * @param keywords
	 * @return
	 */
	public SqlBuilder<T> keywords(String keywords){
		this.keywords=keywords;
		return this;
	}
		
	/**
	 * 设置主键
	 * @param id
	 * @return
	 */
	public SqlBuilder<T> id(Long id){
		this.id=id;
		return this;
	}
	
	/**
	 * 设置主键列表
	 * @param ids
	 * @return
	 */
	public SqlBuilder<T> ids(List<Long> ids){
		this.ids=ids;
		return this;
	}
	
	/**
	 * 设置主键列表
	 * @param ids
	 * @return
	 */
	public SqlBuilder<T> ids(Set<Long> ids){
		this.ids=new ArrayList<>(ids);
		return this;
	}
		
	/**
	 * 设置查询SQL，不做任何处理
	 * @param sql
	 * @return
	 */
	public SqlBuilder<T> query(String sql){
		this.entity.setSql(sql);
		return this;
	}
	
	/**
	 * 设置自定义SQL，支持动态条件处理
	 * @param sql
	 * @return
	 */
	public SqlBuilder<T> sql(String sql){
		// 动态条件处理
		Matcher matcher=conditionRegix.matcher(sql);
		while(matcher.find()) {
			String condition=matcher.group();
			sql=sql.replace(condition, whereCondition(condition));
		}
		
		// 静态参数字段名称处理
		matcher=humpFieldRegix.matcher(sql);
		while(matcher.find()) {
			String condition=matcher.group();
			sql=sql.replace(condition, condition.replaceAll("[A-Z]", "_$0").toUpperCase());
		}
		this.entity.setSql(sql);
		return this;
	}
	
	/**
	 * 设置查询条件
	 * @param where
	 * @return
	 */
	public SqlBuilder<T> where(String where){
		this.where=where;
		return this;
	}
		
	/**
	 * 设置查询参数
	 * @param name
	 * @param value
	 * @return
	 */
	public SqlBuilder<T> where(String name,Object value){
		BeanUtils.setFieldValue(this.params, name, value);
		return this;
	}

	/**
	 * 添加关联查询
	 * @param links
	 * @return
	 */
	public SqlBuilder<T> link(LinkEntity ...links){
		for(LinkEntity link:links){
			this.linkEntitys.add(link);
		}
		return this;
	}
	
	/**
	 * 设置排序字段
	 * @param sort
	 * @return
	 */
	public SqlBuilder<T> sort(Sort ...sort){
		this.sort=sort;
		return this;
	}
		
	/**
	 * 设置主键字段
	 * @param keyField
	 * @return
	 */
	public SqlBuilder<T> key(String keyField){
		this.keyField=keyField;
		return this;
	}
		
	/**
	 * 设置是否需要查询总数
	 * @param needCount
	 * @return
	 */
	public SqlBuilder<T> needCount(boolean needCount){
		this.needCount=needCount;
		return this;
	} 

	/**
	 * 关闭查询忽略
	 * @return
	 */
	public SqlBuilder<T> offQueryIgnore(){
		this.queryIgnore=false;
		return this;
	}
		
	/**
	 * 设置分页页码
	 * @param page
	 * @return
	 */
	public SqlBuilder<T> page(int page){
		this.page=page;
		return this;
	} 
		
	/**
	 * 设置分页每页数量
	 * @param pageSize
	 * @return
	 */
	public SqlBuilder<T> pageSize(int pageSize){
		this.pageSize=pageSize;
		return this;
	} 
		
	/**
	 * 获取分页开始位置
	 * @return
	 */
	public long getStart() {
		return (page - 1) * pageSize;
	}

	/**
	 * 设置数据库模式
	 * @param schema
	 * @return
	 */
	public SqlBuilder<T> schema(String schema){
		this.entity.setSchema(schema);
		return this;
	}
	
}
