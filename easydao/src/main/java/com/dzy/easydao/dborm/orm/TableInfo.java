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
    private String[] ColumnNames;
    private String[] ColumnNamewithID;
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

    /** 成员名-类型
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
        if (ColumnNames==null)
        {
            ColumnNames = new String[mColumns.size()];
            for (int i = 0; i < ColumnNames.length; i++) {
                ColumnNames[i] = mColumns.get(i).getColumnName();
            }
        }
        return ColumnNames;
    }

    public String[] getColumnNamesWithID()
    {
        if (ColumnNamewithID==null)
        {
            ColumnNamewithID = new String[mColumns.size() + 1];
            ColumnNamewithID[0] = "ID";
            for (int i =0;i < mColumns.size() ; i++) {
                ColumnNamewithID[i+1] = mColumns.get(i).getColumnName();
            }
        }

        return ColumnNamewithID;
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
