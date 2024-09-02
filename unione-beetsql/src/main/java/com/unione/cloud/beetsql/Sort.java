package com.unione.cloud.beetsql;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

public class Sort implements Serializable{
	/**
	 * 
	 */
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
