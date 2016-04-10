package com.dzy.easydao.dborm.orm;

import com.dzy.easydao.dborm.SqlGenerate.ColumnItem;
import com.dzy.easydao.dborm.SqlGenerate.TableCreator;

import java.util.Map;

/**
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

        for(Map.Entry<String, Class> vk : mTable.getForeignTables().entrySet())
        {
            TableInfo ftable = TableUtil.intiTable(vk.getValue());
            builder.addForeignKey(vk.getValue().getSimpleName() + "_id", "Integer", ftable.getName(), "ID");
        }


        return builder.Build().getSQL();
    }


    public static String AddColumns(String table, ColumnInfo newColumn)
    {
        //alter table D_BrandService add column a int default 0;


        TableCreator.ColumnBuilder builder = new TableCreator.ColumnBuilder();

        ColumnItem item = new ColumnItem();
        item.setType(newColumn.getDBType());
        item.setColumnName(newColumn.getColumnName());
        item.setUnique(newColumn.isUnique());
        item.setCanNULL(newColumn.isCanNull());
        builder.addColumn(item);

        return "alter table " + table + " add column " + builder.toString();
    }


}
