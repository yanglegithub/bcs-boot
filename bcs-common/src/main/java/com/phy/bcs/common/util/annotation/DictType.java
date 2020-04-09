package com.phy.bcs.common.util.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * bean中文名注解
 */
@Target({METHOD, FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DictType {

    /**
     * 如果是字典类型，请设置字典的name值
     */
    String name() default "";
}
