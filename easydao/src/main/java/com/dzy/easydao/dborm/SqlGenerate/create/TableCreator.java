package com.dzy.easydao.dborm.SqlGenerate.create;

import android.support.annotation.NonNull;

import com.dzy.easydao.dborm.SqlGenerate.ColumnItem;
import com.dzy.easydao.dborm.orm.TypeConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dzysg on 2016/2/25 0025.
 */
public class TableCreator
{

    private StringBuilder mSql;

    private String mTableName;

    private TableCreator(StringBuilder sql, String tableName)
    {
        mSql = sql;
        mTableName = tableName;
    }

    public String getSQL()
    {
        return mSql.toString();
    }

    public static NameBuilder Create()
    {
        return new NameBuilder();
    }

    public static class NameBuilder
    {
        private NameBuilder()
        {

        }
        /**
         * 生成创建表语句
         *
         * @param name 表名
         * @return 列构造器
         */
        public ColumnBuilder Name(String name)
        {
            return new ColumnBuilder(name);
        }
    }


    public static class ColumnBuilder
    {
        private StringBuilder mSql = new StringBuilder();
        private String mTableName;
        private List<ColumnItem> mPrimoryKeys = new ArrayList<>();
        // table-column
        private List<ForeignTable> mForeignInfos = new ArrayList<>();
        private List<ColumnItem> mForeignColumns = new ArrayList<>();

        private ColumnBuilder(String n)
        {
            mTableName = n;
            mSql.append("create table ").append(n).append(" (");
        }

        public ColumnBuilder addColums(List<ColumnItem> list)
        {
            for (ColumnItem item : list) {
                addColumn(item);
            }

            return this;
        }

        public ColumnBuilder addColumn(String columnName, String typeName)
        {
            addColumn(new ColumnItem(columnName,typeName, false, true, false));
            return this;
        }

        /** 添加可空、非唯一，非主键列
         * @param columnName 列名
         * @param javaType 类型
         * @return
         */
        public ColumnBuilder addColumn(String columnName, Class<?> javaType)
        {
            String type = TypeConverter.getTypeSTring(javaType);
            addColumn(new ColumnItem(columnName, type, false, true, false));
            return this;
        }


        /**
         * 增加列
         * @param columnName 列名
         * @param javaType   要存储的java数据类型
         * @param canNull    是否可以为NULL
         * @param unique     是否唯一
         * @param iskey      是否为主键
         * @return builder
         */
        public ColumnBuilder addColumn(String columnName, Class<?> javaType, boolean canNull, boolean unique, boolean iskey)
        {
            String type = TypeConverter.getTypeSTring(javaType);
            addColumn(new ColumnItem(columnName,type, iskey, canNull, unique));
            return this;
        }

        /**
         * 增加非主键列
         *
         * @param columnName 列名
         * @param javaType   要存储的数据类型
         * @param canNull    是否可以为NULL
         * @param unique     是否唯一
         * @return builder
         */
        public ColumnBuilder addColumn(String columnName, Class<?> javaType, boolean canNull, boolean unique)
        {
            String type = TypeConverter.getTypeSTring(javaType);
            addColumn(new ColumnItem(columnName, type, false, canNull, unique));
            return this;
        }

        public ColumnBuilder addColumn(@NonNull ColumnItem item)
        {
            mSql.append(item.getColumnName()).append(" ");
            mSql.append(item.getType()).append(" ");
            if (!item.CanNULL())
                mSql.append("NOT NULL ");
            if (item.isUnique())
                mSql.append("UNIQUE ");

            mSql.append(",");

            if (item.isKey())
                mPrimoryKeys.add(item);
            return this;
        }

        public ColumnBuilder addForeignKey(String columnName, String DBtype,String ForeignTable,String ForeignColumn)
        {
            ColumnItem item = new ColumnItem(columnName,DBtype, false, true, false);
            addColumn(item);
            mForeignColumns.add(item);
            mForeignInfos.add(new ForeignTable(ForeignTable, ForeignColumn));
            return this;
        }





        public TableCreator Build()
        {
            boolean flag = true;

            if (mPrimoryKeys.size() > 0) {
                flag = false;
                mSql.append("primary key(");
                for (ColumnItem item : mPrimoryKeys) {
                    mSql.append(item.getColumnName()).append(",");
                }
                mSql.deleteCharAt(mSql.length() - 1).append(") ");
            }


            if (mForeignColumns.size()>0)
            {
                if (!flag)
                {
                    mSql.append(",");
                }
                flag = false;

                ColumnItem citem = null;
                ForeignTable ftable = null;
                for(int i=0;i<mForeignColumns.size();i++)
                {
                    citem = mForeignColumns.get(i);
                    ftable = mForeignInfos.get(i);


                    mSql.append("foreign key(").append(citem.getColumnName()).append(") ");
                    mSql.append("references ").append(ftable.getTableName());
                    mSql.append("(").append(ftable.getColumnName()).append("),");
                }
                mSql.deleteCharAt(mSql.length() - 1);
            }

            if (flag)
            {
                mSql.deleteCharAt(mSql.length() - 1);
            }

            mSql.append(")");
            return new TableCreator(mSql, mTableName);
        }





        public static class ForeignTable
        {
            String mTableName;
            String mColumnName;

            public ForeignTable(String tableName, String columnName)
            {
                mTableName = tableName;
                mColumnName = columnName;
            }

            public String getTableName()
            {
                return mTableName;
            }

            public void setTableName(String tableName)
            {
                mTableName = tableName;
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


    }


}
