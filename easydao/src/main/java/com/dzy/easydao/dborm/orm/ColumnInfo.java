package com.dzy.easydao.dborm.orm;

/** 列信息实体
 * Created by dzysg on 2016/2/29 0029.
 */
public class ColumnInfo
{
    private String mColumnName;
    private Class mJavatype;
    private String mDBType;
    private String mFeildName;

    private  boolean isKey = false;
    private boolean canNull= true;
    private boolean Unique = false;


    public Class getJavatype()
    {
        return mJavatype;
    }

    public void setJavatype(Class javatype)
    {
        this.mJavatype = javatype;
    }

    public boolean isKey()
    {
        return isKey;
    }

    public void setIsKey(boolean isKey)
    {
        this.isKey = isKey;
    }

    public boolean isCanNull()
    {
        return canNull;
    }

    public void setCanNull(boolean canNull)
    {
        this.canNull = canNull;
    }

    public boolean isUnique()
    {
        return Unique;
    }

    public void setUnique(boolean unique)
    {
        Unique = unique;
    }

    public String getColumnName()
    {
        return mColumnName;
    }

    public void setColumnName(String columnName)
    {
        mColumnName = columnName;
    }



    public String getDBType()
    {
        return mDBType;
    }

    public void setDBType(String DBType)
    {
        mDBType = DBType;
    }

    public String getFeildName()
    {
        return mFeildName;
    }

    public void setFeildName(String feildName)
    {
        mFeildName = feildName;
    }
}
