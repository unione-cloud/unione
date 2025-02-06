package com.unione.cloud.form.data.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unione.cloud.beetsql.DataBaseDao;
import com.unione.cloud.beetsql.builder.SqlBuilder;
import com.unione.cloud.core.dto.Params;
import com.unione.cloud.core.dto.Results;
import com.unione.cloud.core.exception.AssertUtil;
import com.unione.cloud.core.security.SessionService;
import com.unione.cloud.core.util.BeanUtils;
import com.unione.cloud.form.data.dto.DataConvertOption;
import com.unione.cloud.form.data.dto.DataConvertRequest;
import com.unione.cloud.form.data.model.SysDataConvertor;
import com.unione.cloud.form.data.storage.StorageBaseService;
import com.unione.cloud.form.data.storage.model.DataResult;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service("dataConvertorService")
public class DataConvertorService {
	
	@Autowired
	private DataBaseDao dataBaseDao;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private StorageBaseService storageBaseService;
	
	
	public static enum ConvertorType{
		DICT("dict","字典转换"),OPTION("option","静态选项"),DBTABLE("dbtable","数据集转换"),API("api","接口转换");
		
		private ConvertorType(String value,String title) {
			this.value=value;
			this.title=title;
		}
		
		private String value;
		private String title;
		
		public String value() {
			return this.value;
		}
		
		public String title() {
			return this.title;
		}
	}
	
	
	
	/**
	 * 	加载数据选项
	 * @param id
	 * @param request
	 * @return
	 */
	public Results<List<DataConvertOption>> load(Long id,Params<DataConvertRequest> params){
		log.debug("进入：加载数据选项方法,id:{},params:{}",id,params);
		AssertUtil.service().notNull(id, "转换器id不能为空");
		
		SysDataConvertor convertor=dataBaseDao.findById(SqlBuilder.build(SysDataConvertor.class).id(id));
		AssertUtil.service().notNull(convertor, "转换器未找到")
			.notEq(convertor.getStatus(), 1, "转换器已停用");
		
		if(ConvertorType.API.value().equals(convertor.getTypes())) {
			return loadApi(convertor,params);
		}else if(ConvertorType.DBTABLE.value().equals(convertor.getTypes())) {
			return loadDbTable(convertor,params);
		}
		
		return Results.success();
	}
	
	
	/**
	 * 	从远程接口中加载转换数据
	 * @param convertor
	 * @param request
	 * @return
	 */
	public Results<List<DataConvertOption>> loadApi(SysDataConvertor convertor,Params<DataConvertRequest> params){
		log.info("进入：从远程接口中加载转换数据方法,id:{},ds id:{},url:{},request:{}",convertor.getId(),convertor.getDsId(),convertor.getUrl(),params.getBody());
		
		return Results.success();
	}
	
	
	/**
	 * 	从数据table中加载转换数据
	 * @param convertor
	 * @param request
	 * @return
	 */
	public Results<List<DataConvertOption>> loadDbTable(SysDataConvertor convertor,Params<DataConvertRequest> params){
		DataConvertRequest request=params.getBody();
		log.info("进入：从远程接口中加载转换数据方法,id:{},ds id:{},table:{},request:{}",convertor.getId(),convertor.getDsId(),convertor.getTableName(),request);
		AssertUtil.service().notNull(convertor, new String[]{"dsId","tableName","valueField","labelField"},"转换器属性%s丢失");
		if(!ObjectUtil.isEmpty(convertor.getPidField())) {
			AssertUtil.service().notNull(convertor.getIdField(), "树形转换器，id字段名称不能为空");
		}
		
		StringBuffer sql=new StringBuffer();
		StringBuffer field=new StringBuffer();
		sql.append("SELECT ");
		field.append(convertor.getValueField()).append(" as \"value\",")
		     .append(convertor.getLabelField()).append(" as \"label\"");
		if(!ObjectUtil.isEmpty(convertor.getIdField())) {
			field.append(",").append(convertor.getIdField()).append(" as \"id\"");
		}
		if(!ObjectUtil.isEmpty(convertor.getPidField())) {
			field.append(",").append(convertor.getPidField()).append(" as \"pid\"");
		}
		if(!ObjectUtil.isEmpty(convertor.getTableField())) {
			field.append(",").append(convertor.getTableField());
		}
		sql.append(field);
		sql.append(" FROM ").append(convertor.getTableName());
		sql.append(" WHERE 1=1 ");
		if(!ObjectUtil.isEmpty(convertor.getIdField())) {
			sql.append(System.lineSeparator())
			   .append("-- @if(isNotEmpty(params.id)){").append(System.lineSeparator())
			   .append(" AND ").append(convertor.getIdField()).append("=#{params.id}").append(System.lineSeparator())
			   .append("-- @}");
		}
		if(!ObjectUtil.isEmpty(convertor.getPidField())) {
			sql.append(System.lineSeparator())
			   .append("-- @if(isNotEmpty(params.pid)){").append(System.lineSeparator())
			   .append(" AND ").append(convertor.getPidField()).append("=#{params.pid}").append(System.lineSeparator())
			   .append("-- @}");
		}
		sql.append(System.lineSeparator())
		   .append("-- @if(isNotEmpty(params.value) && !isBlank(params.value)){").append(System.lineSeparator())
		   .append(" AND ").append(convertor.getValueField()).append("=#{params.value}").append(System.lineSeparator())
		   .append("-- @}");
		sql.append(System.lineSeparator())
		   .append("-- @if(isNotEmpty(params.keywords) && !isBlank(params.keywords)){").append(System.lineSeparator())
		   .append(" AND (").append(convertor.getValueField()).append(" LIKE #{'%'+params.keywords+'%'} OR ").append(convertor.getLabelField()).append(" LIKE #{'%'+params.keywords+'%'})").append(System.lineSeparator())
		   .append("-- @}");
		
		if(!ObjectUtil.isEmpty(convertor.getTableWhere())) {
			sql.append(System.lineSeparator()).append(convertor.getTableWhere());
		}
		
		Map<String, Object> paramsObj=new HashMap<>();
		paramsObj.put("id",request.getId());
		paramsObj.put("pid",request.getPid());
		paramsObj.put("value",request.getValue());
		paramsObj.put("keywords",request.getKeywords());
		
		paramsObj.put("user",sessionService.getPrincipal());
		paramsObj.put("now", DateUtil.date());
		
		Map<String, Object> ctx=new HashMap<>();
		ctx.put("params", paramsObj);
		List<String> fields=Arrays.asList("id","pid","value","label"); 
		Results<List<DataConvertOption>> result=Results.success();
		if(convertor.isPaging()) {
			// 分页加载
			DataResult<List<Map<String, Object>>> dataResult = storageBaseService.findListPage(convertor.getDsId(), 
					sql.toString(), ctx, params.getPage(), 15);
			result.setTotal(dataResult.getTotal());
			List<DataConvertOption> options = dataResult.getBody().stream().map(row->{
				DataConvertOption option=new DataConvertOption();
				row.keySet().stream().forEach(key->{
					if(fields.contains(key)) {
						BeanUtils.setFieldValue(option, key, row.get(key));
					}else {
						if(option.getProps()==null) {
							option.setProps(new HashMap<>());
						}
						option.getProps().put(key, row.get(key));
					}
				});
				return option;
			}).collect(Collectors.toList());
			result.setBody(options);
		}else {
			// 列表加载
			List<DataConvertOption> options = storageBaseService.findList(convertor.getDsId(), sql.toString(), ctx)
			.stream().map(row->{
				DataConvertOption option=new DataConvertOption();
				row.keySet().stream().forEach(key->{
					if(fields.contains(key)) {
						BeanUtils.setFieldValue(option, key, row.get(key));
					}else {
						if(option.getProps()==null) {
							option.setProps(new HashMap<>());
						}
						option.getProps().put(key, row.get(key));
					}
				});
				return option;
			}).collect(Collectors.toList());
			
			// 树形结构，同步加载，构造属性结构
			if(!ObjectUtil.isEmpty(convertor.getPidField()) && !convertor.isAsync()) {
				List<DataConvertOption> root=new ArrayList<>();
				Map<Long, DataConvertOption> map=new HashMap<>();
				options.stream().forEach(o->{
					map.put(o.getId(), o);
				});
				options.stream().forEach(o->{
					DataConvertOption parent=map.get(o.getPid());
					if(parent!=null) {
						if(parent.getChildren()==null) {
							parent.setChildren(new ArrayList<>());
						}
						parent.getChildren().add(o);
					}else {
						root.add(o);
					}
				});
				options=root;
			}
			
			result.setBody(options);
		}
		
		return result;
	}
	
	
	
	

}
