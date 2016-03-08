package com.dzy.easydao.dborm.orm;

import android.util.Log;

import com.dzy.easydao.dborm.annotation.Column;
import com.dzy.easydao.dborm.annotation.Foreign;
import com.dzy.easydao.dborm.annotation.ID;
import com.dzy.easydao.dborm.annotation.Table;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * Created by dzysg on 2016/2/25 0025.
 */
public class TableUtil
{

    private static ConcurrentMap<Class<?>, TableInfo> mTablesCache = new ConcurrentHashMap<>();


    /** 根据类的注解解析出对应的表名，列名，属性名，field，关联的外表名，关联的属性
     * @param type 实体类类型
     * @return 表信息
     */
    public static TableInfo intiTable(Class<?> type)
    {

        //该类是否已经被解析过
        TableInfo item = mTablesCache.get(type);
        if (item != null)
            return item;

        boolean hasColumn = false, hasId = false;
        item = new TableInfo();
        item.setType(type);

        //获取表名
        Table table = type.getAnnotation(Table.class);
        if (table != null)
            item.setName(table.value());


        Field[] fields = type.getDeclaredFields();
        for(Field field : fields)
        {
            //获取列名，不包括id列和外键列
            Column column = field.getAnnotation(Column.class);
            if (column != null)
            {
                //检查类型是否非法
                if (!CheckType(field.getType()))
                    continue;

                hasColumn = true;
                field.setAccessible(true);
                ColumnInfo info = new ColumnInfo();
                info.setColumnName(column.Name());
                info.setDBType(TypeConverter.getTypeSTring(field.getType()));
                info.setFeildName(field.getName());
                info.setJavatype(field.getType());

                info.setIsKey(column.isKey());
                info.setCanNull(column.CanNull());
                info.setUnique(column.unique());

                item.addColumn(info);
                item.addField(field);
                continue;
            }

            //获取id列
            ID id = field.getAnnotation(ID.class);
            if (id != null)
            {
                //id一定要为整数型
                if (!CheckID(field.getType()))
                    continue;

                hasId = true;
                field.setAccessible(true);

                ColumnInfo info = new ColumnInfo();
                info.setColumnName("ID");
                info.setDBType("Integer");
                info.setFeildName(field.getName());
                info.setJavatype(field.getType());
                item.setID(info);
                item.setIdField(field);
                continue;
            }

            //获取外键列
            Foreign foreign = field.getAnnotation(Foreign.class);
            if (foreign != null)
            {

                TableInfo foreignTable = TableUtil.intiTable(field.getType());
                if (foreignTable != null)
                {
                    if (foreign.TableName().equals(foreignTable.getName()))
                    {
                        foreignTable.setType(field.getType());
                        item.getForeignTables().put(field.getName(),field.getType());
                        mTablesCache.put(field.getType(), foreignTable);
                    }
                    else
                    {
                        Log.e("easydao", "ForeignTableName not equals Tablename");
                    }
                } else
                    Log.e("easydao", "Foreign field init failed");
            }
        }
        if (hasColumn && hasId)
        {
            mTablesCache.put(type, item);
            return item;
        } else
            return null;
    }

    public static boolean CheckType(Class<?> type)
    {
        switch (type.getSimpleName())
        {
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "short":
            case "Short":
            case "String":
            case "float":
            case "double":
            case "bype[]":
                return true;
        }
        return false;
    }

    public static boolean CheckID(Class c)
    {
        switch (c.getSimpleName())
        {
            case "int":
            case "Integer":
            case "long":
            case "Long":
            case "short":
            case "Short":
                return true;
        }
        return false;
    }
}
