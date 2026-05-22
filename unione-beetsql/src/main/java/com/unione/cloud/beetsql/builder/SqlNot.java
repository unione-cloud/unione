package com.unione.cloud.beetsql.builder;

import java.util.Map;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SqlNot {

    private SqlFun fun=SqlFun.AND;

    /**
	 * 	字段名称
	 */
	private String field;
	
	/**
	 * 	比较方式
	 */
	private SqlAction action;

    /**
	 * 	比较值
	 */
	private Object value;

    public void toSql(StringBuffer buffer) {
		// ID,IDS搜索特殊处理
		if(SqlAction.NOT_EQ.equals(this.action)) {
			buffer.append(" ").append(this.fun.name()).append(" ")
			  .append(this.field.replaceAll("[A-Z]", "_$0").toUpperCase()).append(this.action.getAction()).append(this.action.express(String.format("notEq%s", this.field)));
		}else if(SqlAction.NOT_IN.equals(this.action)) {
            buffer.append(" ").append(this.fun.name()).append(" ")
			  .append(this.field.replaceAll("[A-Z]", "_$0").toUpperCase()).append(this.action.getAction()).append(this.action.express(String.format("notIn%s", this.field)));
        }
	}

    public void toParams(Map<String,Object> params) {
        if(SqlAction.NOT_EQ.equals(this.action)) {
            params.put(String.format("notEq%s", this.field), this.value);
        }else if(SqlAction.NOT_IN.equals(this.action)) {
            params.put(String.format("notIn%s", this.field), this.value);
        }
    }

}
