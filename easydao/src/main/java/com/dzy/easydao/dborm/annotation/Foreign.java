package com.dzy.easydao.dborm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  外键关联，只能关联其它表的id列
 * Created by dzysg on 2016/3/2 0002.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Foreign
{

   /**
    * @return 外键关联的表名
    */
   String TableName();
}
