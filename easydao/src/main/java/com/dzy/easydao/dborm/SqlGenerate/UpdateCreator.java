package com.dzy.easydao.dborm.SqlGenerate;

/**
 *
 * Created by dzysg on 2016/3/9 0009.
 */
public class UpdateCreator
{
    StringBuilder sb;
    public UpdateCreator(String Name)
    {
        sb = new StringBuilder("update ").append(Name).append(" set ");
    }

    public static UpdateCreator Update(String name)
    {
        return new UpdateCreator(name);
    }

    public UpdateCreator set(String... columns)
    {
        for(String str:columns)
        {
            sb.append(str).append("=?,");
        }
        sb.deleteCharAt(sb.length()-1).append(" ");
        return this;
    }
    public UpdateCreator where(String c)
    {
        sb.append("where ").append(c).append("=?");
        return this;
    }

    public String Build()
    {
        return sb.toString();
    }



}
