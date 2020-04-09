package com.phy.bcs.common.util;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class EnumUtils {

    private volatile static Map<String, Map<String, Map<String, String>>> allEnumsMap = Maps.newHashMap();
    private volatile static SimpleBeanDefinitionRegistry enumBeanDefinitionRegistry = null;

    /**
     * 将枚举类型定义的值转换为Map
     *
     * @param clazz 枚举类型的类
     * @return Map
     */
    public static Map<String, String> getValuesMap(final Class<? extends IEnum> clazz) {
        Locale locale = LocaleMessageUtils.getCurrentLocale();
        // 为了保证顺序，使用LinkedHashMap而不是HashMap
        final Map<String, String> map = new LinkedHashMap<>();
        final IEnum[] options = clazz.getEnumConstants();
        if (clazz != null) {
            if (options != null) {
                for (final IEnum option : options) {
                    String localeMessageKey = String.join(".", "enums", option.getClass().getSimpleName(), option.toString());
                    String localeMessage = LocaleMessageUtils.get(localeMessageKey, null, locale);
                    map.put(option.getValue().toString(), localeMessageKey.equals(localeMessage) ? option.toString() : localeMessage);
                }
            }
        }
        return map;
    }

    /**
     * 根据value获取desc
     *
     * @param clazz 枚举类型的类
     */
    public static String getDesc(final Class<? extends IEnum> clazz, final String value) {
        return getValuesMap(clazz).get(value);
    }

    public static Map<String, Map<String, String>> getIEnumMap() {
        String currentLocale = LocaleMessageUtils.getCurrentLocale().toString();
        Map<String, Map<String, String>> enumsMap = allEnumsMap.get(currentLocale);
        if (enumsMap == null) {
            enumsMap = initIEnumsMap();
            Assert.notNull(enumsMap, "初始化枚举错误！");
            allEnumsMap.put(currentLocale, enumsMap);
        }
        return enumsMap;
    }

    /**
     * 一次初始化所有枚举类
     */
    private static Map<String, Map<String, String>> initIEnumsMap() {
        Map<String, Map<String, String>> enumsMap = Maps.newHashMap();
        String[] beanDefinitionNames = getIEnumBeanDefinitionNames();
        for (String name : beanDefinitionNames) {
            BeanDefinition beanDefinition = enumBeanDefinitionRegistry.getBeanDefinition(name);

            Class clazz = null;
            try {
                clazz = Class.forName(beanDefinition.getBeanClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            Map<String, String> resultMap = EnumUtils.getValuesMap(clazz);
            enumsMap.put(name, resultMap);
        }
        return enumsMap;
    }

    public static String[] getIEnumBeanDefinitionNames() {
        if (enumBeanDefinitionRegistry == null) {
            synchronized (EnumUtils.class) {
                if (enumBeanDefinitionRegistry == null) {
                    enumBeanDefinitionRegistry = new SimpleBeanDefinitionRegistry();
                    ClassPathBeanDefinitionScanner s = new ClassPathBeanDefinitionScanner(enumBeanDefinitionRegistry, false);
                    TypeFilter tf = new AssignableTypeFilter(IEnum.class);
                    s.setIncludeAnnotationConfig(false);
                    s.addIncludeFilter(tf);
                    s.scan("com.phy.bcs.module");
                }
            }
        }
        return enumBeanDefinitionRegistry.getBeanDefinitionNames();
    }

}
