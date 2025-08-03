package com.unione.cloud.core.util;

import java.beans.PropertyDescriptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;

public class DesensitizeUtil {

    public static enum DesensitizeType{
        TEL,EMAIL,IDCARD,NAME
    }

    /**
     * 数据脱敏处理
     * @param object    对象
     * @param field     字段名称
     * @param type      脱敏类型
     */
    public static void process(Object object,String field,DesensitizeType type){
        if(object==null || field==null || type==null){
            return;
        }
        PropertyDescriptor descriptor=BeanUtil.getPropertyDescriptor(object.getClass(), field);
        if(descriptor==null || !String.class.equals(descriptor.getPropertyType())){
            // 只处理string类型属性
            return;
        }
        Object fieldValue=BeanUtil.getFieldValue(object, field);
        if(ObjectUtil.isEmpty(fieldValue)){
            return;
        }
        String fieldValueStr=fieldValue.toString();
        switch (type){
            case TEL:
                fieldValueStr=fieldValueStr.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
                break;
            case EMAIL:
                fieldValueStr=fieldValueStr.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1$2****$3$4");
                break;
            case IDCARD:
                fieldValueStr=fieldValueStr.replaceAll("(\\d{4})\\d{10}(\\w{4})", "$1********$2");
                break;
            case NAME:
                fieldValueStr=fieldValueStr.replaceAll("(\\w{1})\\w+\\w{1}", "$1****$2");
                break;
        }
        BeanUtil.setFieldValue(object, field, fieldValueStr);
    }


}
