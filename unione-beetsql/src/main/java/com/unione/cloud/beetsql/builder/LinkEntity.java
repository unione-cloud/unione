package com.unione.cloud.beetsql.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.beetl.sql.core.SQLManager;

import com.unione.cloud.core.exception.AssertUtil;

import cn.hutool.core.util.ObjectUtil;
import lombok.Data;

@Data
public class LinkEntity {

     /***
     * 外键字段
     */
    private String field;

    private Class<?> linkEntityClass;

    /***
     * 关联字段，默认：主键关联
     */
    private String linkField;

    /**
     * 关联表名称
     */
    private String linkTableName;

    /***
     * 关联参数
     */
    private Map<String, Object> params=new HashMap<>();

    /***
     * 关联子查询条件
     */
    private List<LinkCondition> linkConditions = new ArrayList<>();

    private List<LinkCondition> optstacks = new ArrayList<>();

    public static LinkEntity build(String field, Class<?> entityClass) {
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.setField(field.replaceAll("[A-Z]", "_$0").toUpperCase());
        linkEntity.setLinkEntityClass(entityClass);
        return linkEntity;
    }

    public static LinkEntity build(String field, Class<?> entityClass, String link) {
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.setLinkEntityClass(entityClass);
        linkEntity.setLinkField(link.replaceAll("[A-Z]", "_$0").toUpperCase());
        linkEntity.setField(field.replaceAll("[A-Z]", "_$0").toUpperCase());
        return linkEntity;
    }

    public static LinkEntity build(String field, String tableName) {
        return build(field, tableName, "id");
    }

    public static LinkEntity build(String field, String tableName, String link) {
        LinkEntity linkEntity = new LinkEntity();
        linkEntity.setLinkTableName(tableName.replaceAll("[A-Z]", "_$0").toUpperCase());
        linkEntity.setLinkField(link.replaceAll("[A-Z]", "_$0").toUpperCase());
        linkEntity.setField(field.replaceAll("[A-Z]", "_$0").toUpperCase());
        return linkEntity;
    }

    public LinkEntity eq(String field, Object value,SqlFun...fun) {
        LinkCondition linkCondition = new LinkCondition();
        linkCondition.setColumn(field);
        linkCondition.setFun(fun.length>0?fun[0]:SqlFun.AND);
        linkCondition.setAction(SqlAction.EQ);
        this.params.put(field, value);
        if (optstacks.isEmpty()) {
            this.linkConditions.add(linkCondition);
        } else {
            optstacks.getLast().getChildrens().add(linkCondition);
        }
        return this;
    }

    public LinkEntity neq(String field, Object value,SqlFun...fun) {
        LinkCondition linkCondition = new LinkCondition();
        linkCondition.setColumn(field);
        linkCondition.setFun(fun.length>0?fun[0]:SqlFun.AND);
        linkCondition.setAction(SqlAction.NEQ);
        this.params.put(field, value);
        if (optstacks.isEmpty()) {
            this.linkConditions.add(linkCondition);
        } else {
            optstacks.getLast().getChildrens().add(linkCondition);
        }
        return this;
    }

    public LinkEntity like(String field, Object value,SqlFun...fun) {
        LinkCondition linkCondition = new LinkCondition();
        linkCondition.setColumn(field);
        linkCondition.setFun(fun.length>0?fun[0]:SqlFun.AND);
        linkCondition.setAction(SqlAction.LIKE);
        this.params.put(field, value);
        if (optstacks.isEmpty()) {
            this.linkConditions.add(linkCondition);
        } else {
            optstacks.getLast().getChildrens().add(linkCondition);
        }
        return this;
    }

    public LinkEntity in(String field, Collection<?> value,SqlFun... fun) {
        LinkCondition linkCondition = new LinkCondition();
        linkCondition.setColumn(field);
        linkCondition.setFun(fun.length>0?fun[0]:SqlFun.AND);
        linkCondition.setAction(SqlAction.IN);
        this.params.put(field, value);
        if (optstacks.isEmpty()) {
            this.linkConditions.add(linkCondition);
        } else {
            optstacks.getLast().getChildrens().add(linkCondition);
        }
        return this;
    }

    public LinkEntity nin(String field, Collection<?> value,SqlFun...fun) {
        LinkCondition linkCondition = new LinkCondition();
        linkCondition.setColumn(field);
        linkCondition.setFun(fun.length>0?fun[0]:SqlFun.AND);
        linkCondition.setAction(SqlAction.NIN);
        this.params.put(field, value);
        if (optstacks.isEmpty()) {
            this.linkConditions.add(linkCondition);
        } else {
            optstacks.getLast().getChildrens().add(linkCondition);
        }
        return this;
    }

    public LinkEntity and(){
        LinkCondition linkCondition = new LinkCondition();
        linkCondition.setFun(SqlFun.AND);
        optstacks.add(linkCondition);
        return this;
    }

    public LinkEntity or(){
        LinkCondition linkCondition = new LinkCondition();
        linkCondition.setFun(SqlFun.OR);
        optstacks.add(linkCondition);
        return this;
    }

    public LinkEntity end(){
        optstacks.removeLast();
        return this;
    }


    public String toSql(SQLManager sqlManager){
        if(ObjectUtil.isEmpty(this.linkTableName)){
            this.linkTableName=sqlManager.getNc().getTableName(this.linkEntityClass);
        }
        if(ObjectUtil.isEmpty(linkField)){
            List<SqlField> fields = SqlKit.loadFields(sqlManager, this.linkEntityClass, this.linkTableName);
            Optional<SqlField> pkField = fields.stream().filter(f -> f.isPk()).findFirst();
            AssertUtil.service().isTrue(pkField.isPresent(), "表" + this.linkTableName + "没有主键字段");
            this.linkField = pkField.get().getColumn();
        }

        StringBuffer sb = new StringBuffer();
        sb.append("-- @if(notNull(").append(field).append("LinkParams)){\n");
        sb.append(" AND ").append(field).append(" IN (\n")
          .append("SELECT ").append(linkField).append(" FROM ").append(linkTableName).append("\n")
          .append("\n-- @sqlWhere(){\n");
        if (!linkConditions.isEmpty()) {
            linkConditions.forEach(linkCondition -> {
                linkCondition.toSql(field,sb);
            });
        }
        sb.append("-- @}\n");
        sb.append(")\n");
        sb.append("-- @}\n");
        return sb.toString();
    }

}
