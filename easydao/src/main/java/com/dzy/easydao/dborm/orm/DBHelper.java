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
        // TODO: 2016/2/29 0029 数据库名称和版本号应由配置文件决定
        if (mSingle==null)
        mSingle = new DBHelper(context, "dZYORM", 1);
        return mSingle;
    }


    public void Init(TableInfo tableInfo)
    {
        SQLiteDatabase db = getWritableDatabase();
        //查询表是否已经被创建过
        //Cursor cursor = db.rawQuery("select name from sqlite_master where name =?", new String[]{tableInfo.getName()});
        db.execSQL(SqlHelper.getCreateTableSql(tableInfo));
        db.close();
        //cursor.close();

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
