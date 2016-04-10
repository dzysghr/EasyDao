package com.dzy.easydao.dborm.SqlGenerate;

/**
 *
 * Created by dzysg on 2016/3/7 0007.
 */
public class InsertBuilder
{
    StringBuilder mSb;

    private InsertBuilder(String tablename)
    {
        mSb = new StringBuilder("insert into ").append(tablename).append("(");//.append(" values( ");
    }

    public static InsertBuilder intoTable(String tb)
    {
        return new InsertBuilder(tb);
    }

    public InsertBuilder ColumnsWithValues(String[] Columns)
    {
        for(int i = 0; i < Columns.length; i++)
        {
            mSb.append(Columns[i]).append(",");
        }
        mSb.deleteCharAt(mSb.length()-1).append(") values(");

        for(int i = 0; i < Columns.length; i++)
        {
            mSb.append("?,");
        }
        mSb.deleteCharAt(mSb.length()-1).append(")");
        return this;
    }
    public InsertBuilder Columns(String[] Columns)
    {
        for(int i = 0; i < Columns.length; i++)
        {
            mSb.append(Columns[i]).append(",");
        }
        mSb.deleteCharAt(mSb.length()-1).append(" ");
        mSb.deleteCharAt(mSb.length()-1).append(")");
        return this;
    }



    public String Build()
    {
        return mSb.toString();
    }

}
