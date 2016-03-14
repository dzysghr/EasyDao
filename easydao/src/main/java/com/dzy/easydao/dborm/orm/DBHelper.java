package com.dzy.easydao.dborm.orm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.dzy.easydao.dborm.SqlGenerate.SqlHelper;


/**
 *
 * Created by ziyue on 2015/7/15 0015.
 */
public class DBHelper extends SQLiteOpenHelper
{
    private String mSqlCreate;
    private static DBHelper mSingle = null;

    private DBHelper(Context context, String name, int Vertion)
    {
        super(context, name, null, Vertion);
    }

    public static synchronized DBHelper getInstance(Context context)
    {
        if (mSingle==null)
        mSingle = new DBHelper(context, "EASYORM", 1);
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
