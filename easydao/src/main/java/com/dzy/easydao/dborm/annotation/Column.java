package com.dzy.easydao.dborm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Created by dzysg on 2016/2/25 0025.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column
{
    String Name();
    boolean isKey() default false;
    boolean CanNull() default true;;
    boolean unique() default false;;
}
