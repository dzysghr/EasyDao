package com.dzy.easydao.dborm.SqlGenerate;

/**
 *
 * Created by dzysg on 2016/3/7 0007.
 */
public class InsertCreator
{
    StringBuilder mSb;

    private InsertCreator(String tablename)
    {
        mSb = new StringBuilder("insert into ").append(tablename).append("(");//.append(" values( ");
    }

    public static InsertCreator Create(String tb)
    {
        return new InsertCreator(tb);
    }

    public  InsertCreator Columns(String[] Columns)
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

    public String Build()
    {
        return mSb.toString();
    }

}
