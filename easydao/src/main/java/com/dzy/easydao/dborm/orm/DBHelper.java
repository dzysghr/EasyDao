package com.dzy.easydao.dborm.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import com.dzy.easydao.dborm.SqlGenerate.SqlHelper;


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
        db.close();
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
