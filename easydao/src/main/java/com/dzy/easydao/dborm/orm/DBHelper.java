package com.dzy.easydao.dborm.orm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.dzy.easydao.dborm.SqlGenerate.InsertBuilder;
import com.dzy.easydao.dborm.SqlGenerate.SelectBuilder;

import java.util.ArrayList;
import java.util.List;


/**
 * DBHelper
 * Created by ziyue on 2015/7/15 0015.
 */
public class DBHelper extends SQLiteOpenHelper
{
    private String mSqlCreate;
    private static DBHelper mSingle = null;

    // TODO: 2016/3/17 0017  暂时为public,测试用
    public DBHelper(Context context, String name, int Vertion)
    {
        super(context, name, null, Vertion);
    }


    public static synchronized DBHelper getInstance(Context context,String dbname,int ver)
    {
        if (mSingle == null)
        {
            mSingle = new DBHelper(context,dbname,ver);
            if (Build.VERSION.SDK_INT >= 16)
                mSingle.setWriteAheadLoggingEnabled(true);

        }
        return mSingle;
    }

    public void Init(TableInfo tableInfo)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(SqlHelper.getCreateTableSql(tableInfo));
        checkChanges(tableInfo,db);
        db.close();
    }

    private void checkChanges(TableInfo tableInfo,SQLiteDatabase db)
    {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableInfo.getName() + ")", null);
        List<ColumnInfo> list = tableInfo.getColumns();
        List<ColumnInfo> newColumn = new ArrayList<>();
        List<String> dbcolumn = new ArrayList<>();
        List<String> commonColumn = new ArrayList<>();
        commonColumn.add("ID");

        while(cursor.moveToNext())
        {
            String cname = cursor.getString(1);
            dbcolumn.add(cname);
        }
        cursor.close();
        dbcolumn.remove("ID");

        for(ColumnInfo info:list)
        {
            if (dbcolumn.contains(info.getColumnName()))
            {
                dbcolumn.remove(info.getColumnName());
                commonColumn.add(info.getColumnName());
            }
            else
            {
                newColumn.add(info);
            }
        }

        String[] oldColumn = commonColumn.toArray(new String[commonColumn.size()]);

        //有列要删除
        if (dbcolumn.size()>0)
        {
            Log.i("easydao","Object's members had changed, "+dbcolumn.size()+" columns will be delete from "+tableInfo.getName());


            //先创建暂时表
            String oldName = tableInfo.getName();
            tableInfo.setName(tableInfo.getName() + "_temp");
            db.execSQL(SqlHelper.getCreateTableSql(tableInfo));


            //向暂时表插入旧表数据
            StringBuilder sb = new StringBuilder();
            sb.append(InsertBuilder.intoTable(tableInfo.getName()).Columns(oldColumn).Build());
            sb.append(" ").append(new SelectBuilder().columns(oldColumn).from(oldName).build());

            Log.i("easydao",sb.toString());
            db.execSQL(sb.toString());


            //删除旧表
            db.execSQL("drop table "+oldName);

            //将暂时表改为旧表名
            db.execSQL("alter table "+tableInfo.getName()+" rename to "+oldName);
            tableInfo.setName(oldName);
            return;
        }

        //有新的列
        if (newColumn.size()>0)
        {
            Log.i("easydao","found new columns,perform alter table :"+tableInfo.getName());

            for(ColumnInfo item:newColumn)
            {
                String sql = SqlHelper.AddColumns(tableInfo.getName(),item);
                db.execSQL(sql);
            }
        }

    }


    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        db.execSQL("PRAGMA foreign_keys = ON");
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }

}
