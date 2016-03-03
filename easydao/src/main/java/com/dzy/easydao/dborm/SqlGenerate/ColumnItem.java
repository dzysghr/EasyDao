package com.dzy.easydao.dborm.SqlGenerate;

/**
 *
 * Created by dzysg on 2016/2/25 0025.
 */
public class ColumnItem
{

    private String mColumnName;
    private boolean isKey = false;
    private boolean unique  = false;
    private boolean canNULL =true;
    private String mType;


    public ColumnItem(String columnName,String type,boolean key,boolean canNULL,boolean unique)
    {
        mColumnName = columnName;
        isKey = key;
        mType = type;
        this.unique = unique;
        this.canNULL = canNULL;
    }
    public ColumnItem()
    {

    }

    public String getType()
    {
        return mType;
    }

    public void setType(String type)
    {
        mType = type;
    }


    public boolean isUnique()
    {
        return unique;
    }

    public void setUnique(boolean unique)
    {
        this.unique = unique;
    }


    public boolean CanNULL()
    {
        return canNULL;
    }

    public void setCanNULL(boolean canNULL)
    {
        this.canNULL = canNULL;
    }

    public boolean isKey()
    {
        return isKey;
    }

    public void setIsKey(boolean isKey)
    {
        this.isKey = isKey;
    }


    public String getColumnName()
    {
        return mColumnName;
    }

    public void setColumnName(String columnName)
    {
        mColumnName = columnName;
    }



}
