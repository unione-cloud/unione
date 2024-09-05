package com.unione.cloud.form.data.storage;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.beetl.sql.clazz.SQLType;
import org.beetl.sql.core.ConnectionSource;
import org.beetl.sql.core.ConnectionSourceHelper;
import org.beetl.sql.core.Interceptor;
import org.beetl.sql.core.SQLBatchReady;
import org.beetl.sql.core.SQLManager;
import org.beetl.sql.core.SQLManagerBuilder;
import org.beetl.sql.core.SQLSource;
import org.beetl.sql.core.SqlId;
import org.beetl.sql.core.UnderlinedNameConversion;
import org.beetl.sql.core.db.DBStyle;
import org.beetl.sql.core.db.DamengStyle;
import org.beetl.sql.core.db.MySqlStyle;
import org.beetl.sql.core.db.OracleStyle;
import org.beetl.sql.core.db.PostgresStyle;
import org.beetl.sql.core.db.SqlServerStyle;
import org.beetl.sql.core.page.DefaultPageRequest;
import org.beetl.sql.core.page.DefaultPageResult;
import org.beetl.sql.core.page.PageRequest;
import org.beetl.sql.ext.DebugInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.security.secret.SecretService;
import com.unione.cloud.form.cache.DataSourceCache;
import com.unione.cloud.form.data.model.SysDataSource;
import com.unione.cloud.form.data.storage.format.BlobFormat;
import com.unione.cloud.form.data.storage.format.ClobFormat;
import com.unione.cloud.form.data.storage.model.DataField;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;



/**
 * 	数据存储基础服务
 * @author Jeking 杨
 */
@Slf4j
@Service
public class StorageBaseService {
	
	/**
     * @数据源对象
     */
    private static ConcurrentHashMap<Long,SQLManager> dsMap=new ConcurrentHashMap<>();
    
	@Autowired
	private DataSourceCache dataSourceCache;

	
    @Autowired
    private SecretService secretService;

    
    /**
     * 	获取数据存储SQLManager对象
     * @param dsId
     * @return
     */
    private SQLManager getSQLManager(Long dsId){
        log.debug("进入->根据数据存储，创建SQLManager，dsId:{}",dsId);
        AssertUtil.service().notNull(dsId, "参数dsId不能为空");

        SQLManager sqlManager = dsMap.get(dsId);
        if(sqlManager!=null) {
        	return sqlManager;
        }
        SysDataSource dataSource = dataSourceCache.load(dsId);
        AssertUtil.service().notNull(dataSource, "数据源信息未找到")
        	.notNull(dataSource.getId(), "数据源信息未找到");
        try {
        	String password=dataSource.getPassword();
        	password=secretService.decrypt(password);
            ConnectionSource source = ConnectionSourceHelper.getSimple(dataSource.getDriverName(),
            		dataSource.getUrl(),dataSource.getUsername(),password);
            SQLManagerBuilder builder = new SQLManagerBuilder(source);
            //命名转化，数据库表和列名下划线风格，转化成Java对应的首字母大写，比如create_time 对应ceateTime
            builder.setNc(new UnderlinedNameConversion());
            //数据库风格
            builder.setDbStyle(this.getDbStyle(dataSource));
            builder.setInters(new Interceptor[]{new DebugInterceptor()});
            sqlManager = builder.build();
            dsMap.put(dsId, sqlManager);
        } catch (ServiceException e) {
            dsMap.remove(dsId);
            log.error("数据源创建失败", e);
            throw e;
        } catch (Exception e) {
            dsMap.remove(dsId);
            log.error("数据源创建失败", e);
            throw new ServiceException("数据源创建失败");
        }

        log.debug("退出->根据数据存储，创建SQLManager，dsId:{},sqlManager:{}",dsId,sqlManager);
        return sqlManager;
    }

    
    /**
     * 	插入一条数据，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @return
     */
    public int insert(Long dsId,String sql,Map<String, Object> params){
        log.debug("进入->执行insert方法,dsId:{},sql:{},params:{}",dsId,sql,params);
        int len = this.getSQLManager(dsId).executeUpdate(sql, params);
        log.debug("退出->执行insert方法,dsId:{},sql:{},params:{},len:{}",dsId,sql,params,len);
        return len;
    }
    
    
    /**
     * 	批量插入数据，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @return
     */
    public int[] insert(Long dsId,String sql,List<Object[]> params){
        log.debug("进入->执行insert方法,dsId:{},sql:{},params:{}",dsId,sql,params);
        int[] len = this.getSQLManager(dsId).executeBatchUpdate(new SQLBatchReady(sql, params));
        log.debug("退出->执行insert方法,dsId:{},sql:{},params:{},len:{}",dsId,sql,params,len);
        return len;
    }
    
    
    /**
     * 	更新数据，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @return
     */
    public int update(Long dsId,String sql,Map<String, Object> params){
        log.debug("进入->执行update方法,dsId:{},sql:{},params:{}",dsId,sql,params);
        int len = this.getSQLManager(dsId).executeUpdate(sql, params);
        log.debug("退出->执行update方法,dsId:{},sql:{},params:{},len:{}",dsId,sql,params,len);
        return len;
    }
    
    
    
    /**
     * 	删除数据，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @return
     */
    public int delete(Long dsId,String sql,Map<String, Object> params){
        log.debug("进入->执行delete方法,dsId:{},sql:{},params:{}",dsId,sql,params);
        int len = this.getSQLManager(dsId).executeUpdate(sql, params);
        log.debug("退出->执行delete方法,dsId:{},sql:{},params:{},len:{}",dsId,sql,params,len);
        return len;
    }
    
    
    
    /**
     * 	查询一条数据，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @return
     */
    @SuppressWarnings({ "unchecked"})
    public Map<String, Object> findOne(Long dsId,String sql,Map<String, Object> params){
        log.debug("进入->执行findOne方法,dsId:{},sql:{},params:{}",dsId,sql,params);
        SQLManager sqlManager=this.getSQLManager(dsId);
        SqlId sqlId = sqlManager.getSqlIdFactory().buildTemplate(sql);
        SQLSource source = sqlManager.getSqlLoader().queryAutoSQL(sqlId);
		if (source == null) {
			source = new SQLSource(sqlId, sql);
			source.setSqlType(SQLType.SELECT);
			sqlManager.getSqlLoader().addSQL(sqlId, source);
		}
        Map<String,Object> result = this.getSQLManager(dsId).selectSingle(sqlId, params,Map.class);
        log.debug("退出->执行findOne方法,dsId:{},sql:{},params:{},result:{}",dsId,sql,params,result);
        return result;
    }
    
    /**
     * 	查询一条数据，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @return
     */
    @SuppressWarnings({ "unchecked"})
    public Map<String, Object> findOne(Long dsId,String sql,Map<String, Object> params,List<DataField> fields){
        log.debug("进入->执行findOne方法,dsId:{},sql:{},params:{}",dsId,sql,params);
        SQLManager sqlManager=this.getSQLManager(dsId);
        SqlId sqlId = sqlManager.getSqlIdFactory().buildTemplate(sql);
        SQLSource source = sqlManager.getSqlLoader().queryAutoSQL(sqlId);
		if (source == null) {
			source = new SQLSource(sqlId, sql);
			source.setSqlType(SQLType.SELECT);
			sqlManager.getSqlLoader().addSQL(sqlId, source);
		}
		
        Map<String,Object> result = this.getSQLManager(dsId).selectSingle(sqlId, params,Map.class);
        
        if(result!=null) {
        	List<Map<String, Object>> rows=rowMapper(Arrays.asList(result), fields);
        	result = rows.get(0);
        }
        
        log.debug("退出->执行findOne方法,dsId:{},sql:{},params:{},result:{}",dsId,sql,params,result);
        return result;
    }
    
    
    /**
     * 	查询唯一数据，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @return
     */
    @SuppressWarnings({ "unchecked"})
    public Map<String, Object> findUnique(Long dsId,String sql,Map<String, Object> params){
        log.debug("进入->执行findOne方法,dsId:{},sql:{},params:{}",dsId,sql,params);
        SQLManager sqlManager=this.getSQLManager(dsId);
        SqlId sqlId = sqlManager.getSqlIdFactory().buildTemplate(sql);
        SQLSource source = sqlManager.getSqlLoader().queryAutoSQL(sqlId);
		if (source == null) {
			source = new SQLSource(sqlId, sql);
			source.setSqlType(SQLType.SELECT);
			sqlManager.getSqlLoader().addSQL(sqlId, source);
		}
		
        Map<String,Object> result = this.getSQLManager(dsId).selectUnique(sqlId, params,Map.class);
        log.debug("退出->执行findOne方法,dsId:{},sql:{},params:{},result:{}",dsId,sql,params,result);
        return result;
    }
    
    
    /**
     * 	统计数据，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @return
     */
    public Long count(Long dsId,String sql,Map<String, Object> params){
        log.debug("进入->执行count方法,dsId:{},sql:{},params:{}",dsId,sql,params);
        Long result=null;
        List<Long> list = this.getSQLManager(dsId).execute(sql, Long.class, params);
        if(!list.isEmpty()) {
        	result=list.get(0);
        }
        log.debug("退出->执行count方法,dsId:{},sql:{},params:{},result:{}",dsId,sql,params,result);
        return result;
    }
    
    
    /**
     * 	查询数据列表，未分页，指定存储
     * @param sql
     * @param params
     * @return
     */
    @SuppressWarnings({ "rawtypes" })
    public List<Map<String, Object>> findList(Long dsId,String sql,Map<String, Object> params,List<DataField> fields){
        log.debug("进入->执行findList方法,dsId:{},sql:{},params:{}",dsId,sql,params);

        List<Map> result = this.getSQLManager(dsId).execute(sql, Map.class, params);
        List<Map<String, Object>> rows=rowMapper(result, fields);

        log.debug("退出->执行findList方法,dsId:{},sql:{},params:{},result:{}",dsId,sql,params,result);
        return rows;
    }

    /**
     * 	查询数据列表，未分页，指定存储
     * @param sql
     * @param params
     * @return
     */
    @SuppressWarnings({ "rawtypes" })
    public List<Map<String, Object>> findList(Long dsId,String sql,Map<String, Object> params){
        log.debug("进入->执行findList方法,dsId:{},sql:{},params:{}",dsId,sql,params);

        List<Map> result = this.getSQLManager(dsId).execute(sql, Map.class, params);
        List<Map<String, Object>> rows=result.stream().map(row->{
        	Map<String, Object> r=new HashMap<>();
        	for(Object entryObj:row.entrySet()) {
        		Entry entry=(Entry)entryObj;
        		r.put(entry.getKey().toString(), entry.getValue());
        	}
        	return r;
        }).collect(Collectors.toList());

        log.debug("退出->执行findList方法,dsId:{},sql:{},params:{},result:{}",dsId,sql,params,result);
        return rows;
    }

    
    
    
    /**
     * 	分页查询数据列表，未分页，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @param page
     * @param size
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Results<List<Map<String, Object>>> findListPage(Long dsId, String sql,Map<String, Object> params,int page, int size){
        log.debug("进入->执行findListPage方法,dsId:{},sql:{},params:{},page:{},size:{}",dsId,sql,params,page,size);
        Results<List<Map<String, Object>>> result=Results.success();
        
        PageRequest request = DefaultPageRequest.of(page,size);
        DefaultPageResult<Map> pageResult = (DefaultPageResult<Map>)this.getSQLManager(dsId).executePageQuery(sql, Map.class, params,request);
        List<Map<String, Object>> rows=pageResult.getList().stream().map(row->{
        	Map<String, Object> r=new HashMap<>();
        	for(Object entryObj:row.entrySet()) {
        		Entry entry=(Entry)entryObj;
        		r.put(entry.getKey().toString(), entry.getValue());
        	}
        	return r;
        }).collect(Collectors.toList());

        result.setTotal(pageResult.getTotalRow());
        result.setBody(rows);
        log.debug("退出->执行findListPage方法,dsId:{},sql:{},params:{},size:{}",dsId,sql,params,pageResult.getList().size());
        return result;
    }
    
    
    /**
     *	 分页查询数据列表，未分页，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @param page
     * @param size
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Map<String, Object>> findListForPage(Long dsId, String sql,Map<String, Object> params,int page, int size){
        log.debug("进入->执行findListForPage方法,dsId:{},sql:{},params:{},page:{},size:{}",dsId,sql,params,page,size);
        
        PageRequest request = DefaultPageRequest.of(page,size,false);
        DefaultPageResult<Map> pageResult = (DefaultPageResult<Map>)this.getSQLManager(dsId).executePageQuery(sql, Map.class, params,request);
        List<Map<String, Object>> rows=pageResult.getList().stream().map(row->{
        	Map<String, Object> r=new HashMap<>();
        	for(Object entryObj:row.entrySet()) {
        		Entry entry=(Entry)entryObj;
        		r.put(entry.getKey().toString(), entry.getValue());
        	}
        	return r;
        }).collect(Collectors.toList());

        log.debug("退出->执行findListPage方法,dsId:{},sql:{},params:{},size:{}",dsId,sql,params,pageResult.getList().size());
        return rows;
    }
    
    
    /**
     * 	分页查询数据列表，未分页，指定存储
     * @param dsId
     * @param sql
     * @param params
     * @param page
     * @param size
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Results<List<Map<String, Object>>> findListPage(Long dsId, String sql,Map<String, Object> params,int page, int size,List<DataField> fields){
        log.debug("进入->执行findListPage方法,dsId:{},sql:{},params:{},page:{},size:{}",dsId,sql,params,page,size);
        Results<List<Map<String, Object>>> result=Results.success();
        
        PageRequest request = DefaultPageRequest.of(page,size);
        DefaultPageResult<Map> pageResult = (DefaultPageResult<Map>)this.getSQLManager(dsId).executePageQuery(sql, Map.class, params,request);
        List<Map<String, Object>> rows=rowMapper(pageResult.getList(), fields);

        result.setTotal(pageResult.getTotalRow());
        result.setBody(rows);
        log.debug("退出->执行findListPage方法,dsId:{},sql:{},params:{},size:{}",dsId,sql,params,pageResult.getList().size());
        return result;
    }
    
    
    
    /**
     * 分页查询数据列表，未分页，用户所属存储
     * @param sql
     * @param params
     * @param page
     * @param size
     * @param fields
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Map<String, Object>> findListForPage(Long dsId, String sql,Map<String, Object> params,int page, int size,List<DataField> fields){
        log.debug("进入->执行findListForPage方法,dsId:{},sql:{},params:{},page:{},size:{}",dsId,sql,params,page,size);
        
        PageRequest request = DefaultPageRequest.of(page,size,false);
        DefaultPageResult<Map> pageResult = (DefaultPageResult<Map>)this.getSQLManager(dsId).executePageQuery(sql, Map.class, params,request);
        List<Map<String, Object>> rows=rowMapper(pageResult.getList(), fields);

        log.debug("退出->执行findListForPage方法,dsId:{},sql:{},params:{},size:{}",dsId,sql,params,pageResult.getList().size());
        return rows;
    }
    
    
    /**
     * 	数据转换处理
     * @param rows
     * @param fields
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public List<Map<String, Object>> rowMapper(List<Map> rows,List<DataField> fields){
        if(rows==null || rows.isEmpty()) {
            return new ArrayList<>();
        }

        // 字段转换处理
        Map<String, Format> formatMap=new HashMap<>();
        Map<String, DataField> fieldMap=new HashMap<>();
        for(DataField field:fields) {
            // 数据格式化处理
            if(!StringUtils.isEmpty(field.getDataFormat())) {
                String formate = StringUtils.trim(field.getDataFormat());
                field.setDataFormat(formate);
                if(formatMap.get(formate)==null) {
                    if("Date".equalsIgnoreCase(field.getDataType()) || "Timestamp".equalsIgnoreCase(field.getDataType())) {
                        formatMap.put(field.getName(), new SimpleDateFormat(formate.replaceAll("DD", "dd"), Locale.CHINA));
                    }else if("Clob".equalsIgnoreCase(field.getDataType())){
                        formatMap.put(field.getName(), new ClobFormat());
                    }else if("Blob".equalsIgnoreCase(field.getDataType())){
                        formatMap.put(field.getName(), new BlobFormat());
                    }else {
                        formatMap.put(field.getName(), new DecimalFormat(formate));
                    }
                }
            }
            if("Date".equals(field.getDataType()) || "Timestamp".equals(field.getDataType())) {
            	if(formatMap.get(field.getName())==null) {
            		formatMap.put(field.getName(), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            	}
            }
            fieldMap.put(field.getName(), field);
        }

        // 迭代数据，row mapper处理
        return rows.stream().map(row -> {
            Map<String, Object> r = new HashMap<>();
            r.putAll(row);
            if(!formatMap.keySet().isEmpty()) {
            	formatMap.keySet().stream().forEach(key -> {
            		Object value = row.get(key);
            		if (value != null) {
            			Format format = formatMap.get(key);
            			if (format != null) {
            				try {
								value = format.format(value);
							} catch (Exception e) {
								DataField field = fieldMap.get(key);
								log.error("field value format 格式失败,data model id:{},field:{},value:{},format:{}",field.getModelId(),key,value,field.getDataFormat(),e);
							}
            			}
            			if(value instanceof Date) {
            				value = DateUtil.format((Date)value, "yyyy-MM-dd HH:mm:ss");
            			}
            			r.put(key, value);
            		}
            	});
            }
            return r;
        }).collect(Collectors.toList());
    }

    /**
     * 	获取数据存储类型
     * @param dataSource
     * @return
     */
    public DBStyle getDbStyle(SysDataSource dataSource) {
        AssertUtil.service().notNull(dataSource.getDsType(),"dsType不能为空");

        // 资源类型 字典 DATASOURCETYPE ，1mysql，2oracle，3SqlServer，4SqlServer2005，5达梦，
        // 6postgreSql，41excel，61rest说明：1-10关系型数据源，21-40noSql数据源，41-60文件型数据源，61-80接口型数据源
        switch (dataSource.getDsType()) {
            case 1:
                return new MySqlStyle();
            case 2:
                return new OracleStyle();
            case 3:
                return new SqlServerStyle();
            case 4:
                return new SqlServerStyle();
            case 5:
                return new DamengStyle();
            case 6:
                return new PostgresStyle();
            default:
                throw new ServiceException("暂不支持的数据库类型,ds type:"+dataSource.getDsType());
        }
    }

}
