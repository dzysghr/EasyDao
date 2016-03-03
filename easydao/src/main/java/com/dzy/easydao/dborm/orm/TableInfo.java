package com.dzy.easydao.dborm.orm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 表信息类
 * Created by dzysg on 2016/2/25 0025.
 */
public class TableInfo
{
    private String name;

    /**
     * 不包括 id 列，外键列
     */
    private List<ColumnInfo> mColumns = new ArrayList<>();

    private ColumnInfo ID;
    private List<Field> mFields = new ArrayList<>();
    private Field mIdField;

    /**
     *  属性名-属性类型
     */
    private Map<String, Class> mForeignTables = new HashMap<>();
    private Class mType;

    public Class getType()
    {
        return mType;
    }

    public void setType(Class type)
    {
        mType = type;
    }

    public boolean haveForeign()
    {
        return mForeignTables.size()>0;
    }


    /** 属性-类型
     * @return map
     */
    public Map<String,Class> getForeignTables()
    {
        return mForeignTables;
    }


    public void addField(Field f)
    {
        mFields.add(f);
    }

    public List<Field> getFields()
    {
        return mFields;
    }

    public void setIdField(Field idField)
    {
        mIdField = idField;
    }

    public Field getIdField()
    {
        return mIdField;
    }

    public List<ColumnInfo> getColumns()
    {
        return mColumns;
    }

    public String[] getColumnNames()
    {
        String[] names = new String[mColumns.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = mColumns.get(i).getColumnName();
        }
        return names;
    }

    public String[] getColumnNamesWithID()
    {
        String[] names = new String[mColumns.size() + 1];

        for (int i = 0; i < names.length; i++) {
            if (i == 0)
                names[0] = "ID";
            else
                names[i] = mColumns.get(i).getColumnName();
        }
        return names;
    }


    public void addColumn(ColumnInfo c)
    {
        mColumns.add(c);
    }

    public ColumnInfo getID()
    {
        return ID;
    }

    public void setID(ColumnInfo ID)
    {
        this.ID = ID;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
