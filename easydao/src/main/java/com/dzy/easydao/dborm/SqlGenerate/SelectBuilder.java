package com.dzy.easydao.dborm.SqlGenerate;

/** 查询语句生成
 * Created by dzysg on 2016/4/10 0010.
 */
public class SelectBuilder
{

    StringBuilder sb = new StringBuilder();



    public SelectBuilder()
    {
        sb.append("select ");
    }

    public  SelectBuilder from(String tbName)
    {
        sb.deleteCharAt(sb.length()-1);
        sb.append(" from ").append(tbName).append(" ");
        return this;
    }

    public SelectBuilder column(String columnName)
    {
        sb.append(columnName).append(",");

        return this;
    }

    public SelectBuilder columns(String[] columnNames)
    {
        for(String item:columnNames)
        {
            sb.append(item).append(" ,");
        }
        return this;
    }

    public SelectBuilder where(String condition)
    {
        sb.delete(sb.length() - 2, sb.length());
        sb.append("where ");
        sb.append(condition);
        return this;
    }


    public String build()
    {
        return sb.toString();
    }

}
