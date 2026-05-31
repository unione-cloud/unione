package com.unione.cloud.util;

import java.util.Map;

import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;

import com.unione.cloud.core.exception.ServiceException;

import lombok.extern.slf4j.Slf4j;


/**
 * Beetl工具类
 */
@Slf4j
public class BeetlUtil {


    private static GroupTemplate stringTemplate;
    static {
        try {
            StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
            Configuration configuration = Configuration.defaultConfiguration();
            configuration.setCharset("UTF-8");
            stringTemplate = new GroupTemplate(resourceLoader, configuration);
        } catch (Exception e) {
            log.error("Beetl String Template初始化失败", e);
        }
    }


    /**
     * 渲染模版
     * @param tmpl
     * @param data
     * @return
     */
    public static String render(String tmpl, Map<String, Object> data) {
        try {
            Template template = stringTemplate.getTemplate(tmpl);
            template.binding(data);
            return template.render();
        } catch (Exception e) {
            log.error("string template render error,tmpl:{},data:{}", tmpl,data, e);
            throw new ServiceException("string template render error", e);
        }
    }

    /**
     * 校验模版
     * @param tmpl
     * @param data
     * @return
     */
    public static boolean validate(String tmpl, Map<String, Object> data) {
        try {
            Template template = stringTemplate.getTemplate(tmpl);
            template.binding(data);
            template.validate();
        } catch (Exception e) {
            log.error("string template validate error,tmpl:{},data:{}", tmpl,data, e);
            return false;
        }
        return true;
    }

}
