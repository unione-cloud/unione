package com.unione.cloud.core.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * @描述 系统请求DTO
 * @author Jeking Yang
 * @since 1.0.0
 */
@Data
@Slf4j
@Accessors(chain = true)
@Schema(description = "系統请求DTO，排序字段，排序方式都需要进行转换，不能直接输出到SQL")
public class Params<T> implements Serializable {

	/**
	 * 序列化编号.
	 */
	private static final long serialVersionUID = 3885702483311119528L;
	
	/**
	 * 	主键id查询
	 */
	@Schema(title="主键id",hidden = true)
	private Long id;
	
	/**
	 * 	主键id集合查询
	 */
	@Schema(title="主键id集合",hidden = true)
	private List<Long> ids;
	
	/**
	 * 	关键字查询
	 */
	@Schema(title="关键字查询",hidden = true)
	private String keywords;

	/**
	 * 	分页大小
	 */
	@Schema(title = "分页大小",description = "默认每页10条记录", example = "10")
	private int pageSize = 10;

	/**
	 * 	分页页码
	 */
	@Schema(title = "当前页", example = "1")
	private int page = 1;
	
	/**
	 *	总记录数
	 */
	@Schema(hidden=true)
	private Integer total;

	@Schema(title="是否需要count统计",description = "前端查询条件无变化时，可以传入false，减少count统计时间消耗")
	private boolean needCount=true;
	
	@Schema(title = "排序方式")
	private List<Sort> sorts;

	@Schema(title = "请求参数")
	private T body;

	/**
	 * 创建参数DTO实例
	 * @param <P>
	 * @param cls
	 * @return
	 */
	public static <P> Params<P> build(Class<P> cls){
		try {
			P body=(P)cls.getDeclaredConstructor().newInstance();
			Params<P> param=new Params<P>();
			param.setBody(body);
			return param;
		} catch (Exception e) {
			log.error("构建参数对象异常",e);
			throw new RuntimeException("构建参数对象异常");
		}
	}
	
	/**
	 * 创建参数DTO实例
	 * @param <P>
	 * @param body
	 * @return
	 */
	public static <P> Params<P> build(P body){
		Params<P> params=new Params<P>();
		params.setBody(body);
		return params;
	}

	/**
	 * @return the start
	 */
	@JsonIgnore
	public int getStart() {
		return (page - 1) * pageSize;
	}
	
	@JsonIgnore
	public String getSortText() {
		if(sorts==null || sorts.isEmpty()) {
			return null;
		}
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < sorts.size(); ++i) {
			buf.append(sorts.get(i));
			if (i < sorts.size() - 1) {
				buf.append(",");
			}
		}
		return buf.toString();
	}
	
	/**
	 * 添加排序
	 * @param name
	 * @param asc
	 */
	public void addSort(String name,boolean asc) {
		Sort sort=new Sort();
		sort.setAsc(asc);
		sort.setName(name);
		if(sorts==null) {
			sorts=new ArrayList<>();
		}
		sorts.add(sort);
	}
	
	@Data
	public static class Sort{
		@Schema(title="排序字段名称",description = "字段名称eq匹配，不匹配的排序忽略")
		private String name;
		@Schema(title="是否升序排序")
		private boolean asc;
		
		public static Sort build(String name,boolean asc) {
			Sort sort=new Sort();
			sort.setName(name);
			sort.setAsc(asc);
			return sort;
		}
		
		public String getName() {
			if(name!=null && name.matches("[a-zA-Z0-9]*")) {
				return name;
			}
			return null;
		}
		
		public String toString() {
			return String.format("%s %s", this.getName(), asc?"asc":"desc");
		}
	}
	
}
