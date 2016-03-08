package com.dzy.easydao.dborm.SqlGenerate;

import com.dzy.easydao.dborm.SqlGenerate.create.TableCreator;
import com.dzy.easydao.dborm.orm.ColumnInfo;
import com.dzy.easydao.dborm.orm.TableUtil;
import com.dzy.easydao.dborm.orm.TableInfo;

import java.util.Map;

/**
 *
 * Created by dzysg on 2016/3/3 0003.
 */
public class SqlHelper
{
    public static String getCreateTableSql(TableInfo mTable)
    {
        //开始构造create table 语句
        TableCreator.ColumnBuilder builder = TableCreator.Create().Name(mTable.getName());

        builder.addColumn(mTable.getID().getColumnName(), mTable.getID().getDBType() + " primary key autoincrement");
        for(ColumnInfo info : mTable.getColumns())
        {
            ColumnItem item = new ColumnItem();
            item.setType(info.getDBType());
            item.setColumnName(info.getColumnName());
            item.setUnique(info.isUnique());
            item.setCanNULL(info.isCanNull());
            builder.addColumn(item);
        }
        //// TODO: 2016/3/2 0002 增加外键列

        for(Map.Entry<String,Class> vk:mTable.getForeignTables().entrySet())
        {
            TableInfo ftable = TableUtil.intiTable(vk.getValue());
            builder.addForeignKey(vk.getValue().getSimpleName()+"_id","Integer",ftable.getName(),"ID");
        }


        return  builder.Build().getSQL();
    }
}
