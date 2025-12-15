package com.unione.cloud.util;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;

import com.unione.cloud.core.exception.ServiceException;
import com.unione.cloud.core.util.BeanUtils;

import cn.hutool.core.util.ObjectUtil;

public class MapUtil extends cn.hutool.core.map.MapUtil {

    /**
     * 获取map值
     * @param map        目标map
     * @param key        键，支持多级，如：a.b.c
     * @return 值
     */
    public static Object getValue(Map<?, ?> map, String key) {
        return MapUtil.getValue(map, key, null);
    }

    /**
     * 获取map值
     * @param map        目标map
     * @param key        键，支持多级，如：a.b.c
     * @param defaultValue  默认值
     * @return 值
     */
    public static Object getValue(Map<?, ?> map, String key, Object defaultValue) {
        if (ObjectUtil.isEmpty(map)) {
            return defaultValue;
        }
        String pp[] = key.split("\\.");
        if (pp.length == 1) {
            return BeanUtils.getFieldValue(map, key);
        }
        Object m = map;
        for (int i = 0; i < pp.length - 1; i++) {
            m = BeanUtils.getFieldValue(m, pp[i]);
            if (ObjectUtil.isEmpty(m)) {
                return defaultValue;
            }
        }
        return BeanUtils.getFieldValue(m, pp[pp.length - 1]);
    }

    /**
     * 设置map值
     * @param map    目标map
     * @param key    键，支持多级，如：a.b.c
     * @param value  值
     */
    public static void setValue(Map<?, ?> map, String key, Object value) {
        if (ObjectUtil.isEmpty(map)) {
            return;
        }
        String pp[] = key.split("\\.");
        if (pp.length == 1) {
            BeanUtils.setFieldValue(map, key, value);
            return;
        }
        Object m = map;
        for (int i = 0; i < pp.length - 1; i++) {
            Object tmp = BeanUtils.getFieldValue(m, pp[i]);
            if (tmp == null) {
                PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(m.getClass(), pp[i]);
                if (pd == null) {
                    tmp = new HashMap<>();
                } else {
                    try {
                        tmp = pd.getPropertyType().getConstructor().newInstance();
                    } catch (Exception e) {
                        throw new ServiceException("创建对象失败",e);
                    }
                }
            }
            m=tmp;
        }
        BeanUtils.setFieldValue(m, pp[pp.length - 1], value);
    }

}
