package com.dzy.easydao.dborm.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Build;
import android.util.Log;

import com.dzy.easydao.dborm.SqlGenerate.InsertCreator;
import com.dzy.easydao.dborm.SqlGenerate.UpdateCreator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 增删查改基本操作类
 * Created by dzysg on 2016/2/29 0029.
 */
public class EasyDAO<T>
{
    private static ConcurrentMap<Class<?>, EasyDAO> mDAOMap = new ConcurrentHashMap<>();

    private DBHelper mHelper;
    private TableInfo mTable;
    private Class<?> mDaoType;
    private SQLiteDatabase mWriteDb;
    private SQLiteDatabase mReadDb;
    private Util<T> mUtil;
    private static Context mContext;
    private static  String mDBname;
    private static int DBVersion;
    private static boolean inited = false;


    /** 初始化EasyDao，在使用之前确保调用本方法初始化，一次运行且只需调用一次
     * @param c Context ,activity 或者 App都可以
     * @param databasename 数据库名字
     * @param ver 版本
     */
    public static void init(Context c,String databasename,int ver)
    {
        if (inited)
        {
            Log.e("easydao","you had init before");
            return;
        }

        mContext = c.getApplicationContext();
        mDBname = databasename;
        DBVersion = ver;
        inited=true;
    }

    /** 获取一个对象相关的DAO，同一类型全局只有一个
     * @param type 对象类型
     * @param <type> 对象类型
     * @return EasyDao实例
     */
    @SuppressWarnings("unchecked")
    public static synchronized <type> EasyDAO<type> getInstance(Class<type> type)
    {
        EasyDAO<type> dao = mDAOMap.get(type);
        if (dao == null)
        {
            TableInfo tableInfo = TableUtil.intiTable(type);
            if (tableInfo != null)
            {
                dao = new EasyDAO<>(type, tableInfo);
                mDAOMap.put(type, dao);
            } else
            {
                Log.e("easydao", "error type: " + type.getSimpleName() + ",please check the annotation");
                return null;
            }
        }

        return dao;
    }

    private EasyDAO(Class<?> type, TableInfo table)
    {
        mTable = table;
        mHelper = DBHelper.getInstance(mContext,mDBname,DBVersion);
        mHelper.Init(table);
        mDaoType = type;
        mUtil = new Util<>(table, type);
    }

    public TableInfo getTable()
    {
        return mTable;
    }

    public void closeDB()
    {

        if (mWriteDb != null && mWriteDb.isOpen())
        {
            mWriteDb.close();
            mWriteDb = null;
        }


        if (mReadDb != null && mReadDb.isOpen())
        {
            mReadDb.close();
            mReadDb = null;
        }

    }

    public SQLiteDatabase getWritableDb()
    {
        if (mWriteDb == null||!mWriteDb.isOpen())
            mWriteDb = mHelper.getWritableDatabase();
        return mWriteDb;
    }

    private SQLiteDatabase getReadableDb()
    {
        if (mReadDb == null||!mReadDb.isOpen())
            mReadDb = mHelper.getReadableDatabase();
        return mReadDb;
    }

    /**
     * 检查类型一致
     *
     * @param ob 实体
     * @return 一致与否
     */
    private boolean checkClass(Object ob)
    {

        return ob.getClass() == mDaoType;
    }


    /** 统计符合条件的对象个数
     * @param selection 条件语句，如 name=?，可空
     * @param arg 参数值，顺序要与问号一致，可空
     * @return 个数
     */
    public long Count(String selection,String... arg)
    {
        SQLiteDatabase db = getReadableDb();
        return DatabaseUtils.longForQuery(db,selection,arg);
    }




    /** 返回符合条件的第一行数据
     * @param selection 选择条件,用问号代替参数，列如 name=?
     * @param arg 问号对应的值，顺序要与selection一致
     * @return 符合条件的第一个对象
     */
    public T queryFirst(String selection, String... arg)
    {
        SQLiteDatabase db = getReadableDb();
        Cursor cursor = null;
        try
        {
            cursor = db.query(mTable.getName(), null, selection, arg, null, null, null, null);
            if (cursor.moveToNext())
            {
                //实例化对象并赋值
                T t = mUtil.LoadInstance(cursor);
                if (mTable.haveForeign())
                {
                    //加载关联的成员
                    LoadForeignObject(t);
                }
                return t;
            }
        }
        catch (Exception e)
        {
            Log.e("easydao", e.getMessage());
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * 按条件查询
     * @param selection 查询条件 ，如name=?
     * @param arg       查询参数
     * @return list<T>
     */
    public List<T> queryWhere(String selection, String... arg)
    {
        return queryWhere(selection, arg, null, null);
    }

    /**
     * 按条件查询
     * @param selection 查询条件 ，如name=?
     * @param arg       查询参数
     * @return list<T>
     */
    public List<T> queryWheres(String selection, String... arg)
    {
        return queryWhere(selection, arg, null, null);
    }


    public T queryById(long id)
    {
        return queryFirst("ID=?", new String[]{String.valueOf(id)});
    }


    /** 查询对象
     * @param selection 查询条件，如 age < ? and name like ?
     * @param arg 查询条件的参数值，顺序与问号一致
     * @param orderby 结果按所给的列名排序
     * @param limit 筛选位置，如 1，10 表示返回第1个到第10个对象
     * @return 对象集合
     */
    public List<T> queryWhere(String selection, String[] arg, String orderby, String limit)
    {
        SQLiteDatabase db = getReadableDb();

        Cursor cursor = db.query(mTable.getName(), null, selection, arg, null, null, orderby, limit);

        List<T> list = new ArrayList<>();
        try
        {
            while (cursor.moveToNext())
            {
                T t = mUtil.LoadInstance(cursor);
                if (mTable.haveForeign())
                {
                    LoadForeignObject(t);
                }
                list.add(t);
            }
        }
        catch (Exception e)
        {
            Log.e("easydao", e.getMessage());
        }
        cursor.close();
        return list;
    }

    /**
     * 插入新的行，插入后对象 id 被更新
     *
     * @return 成功与否
     */
    public boolean insertNew(Collection<T> list)
    {
        SQLiteDatabase db = getWritableDb();
        String sql = InsertCreator.Create(mTable.getName()).Columns(mTable.getColumnNames()).Build();
        SQLiteStatement statement = db.compileStatement(sql);
        try
        {
            db.beginTransaction();
            for(T item :list)
            {
                statement.clearBindings();
                performInsertNew(item, statement);
            }
            db.setTransactionSuccessful();
            return true;
        }
        catch (Exception e)
        {
            Log.e("easydao", e.getMessage());
            return false;
        }
        finally
        {
            db.endTransaction();
        }

    }

    /**
     * 插入新的行，插入后对象 id 被更新
     *
     * @param ob 要插入的对象
     * @return 成功与否
     */
    public boolean insertNew(T ob)
    {
        try
        {
            SQLiteStatement statement = getWritableDb().compileStatement(InsertCreator.Create(mTable.getName()).Columns(mTable.getColumnNames()).Build());
            performInsertNew(ob, statement);
            return true;
        }
        catch (Exception e)
        {
            Log.e("easydao", e.getMessage());
        }
        return false;
    }


    /** 保存一组对象，参见{@link EasyDAO#save(Object)}
     * @param list
     */
    public void save(Collection<T> list)
    {



        if (Build.VERSION.SDK_INT >= 16)
            getWritableDb().beginTransactionNonExclusive();
        else
            getWritableDb().beginTransaction();
        try
        {
            for(T item : list)
            {
                save(item);
            }
            getWritableDb().setTransactionSuccessful();
        }
        finally
        {
            getWritableDb().endTransaction();
        }
    }


    /**
     * 保存一个对象，若对象 id存在（id>0则算为存在）,则更新数据，否则插入新行
     * @param ob 要更新的对象
     * @return 成功与否
     */
    public boolean save(T ob)
    {
        if (!checkClass(ob))
            return false;

        long id = mUtil.getId(ob);
        try
        {
            if (id < 1)
            {
                SQLiteStatement statement = getWritableDb().compileStatement(InsertCreator.Create(mTable.getName()).Columns(mTable.getColumnNames()).Build());
                performInsertNew(ob, statement);
            } else if (exist(ob))
                performUpdate(ob);
            else
                performInsert(ob);
            return true;
        }
        catch (Exception e)
        {
            Log.e("easyDao", e.getMessage());
        }
        return false;
    }

    public void deleteWhere(String selection,String... arg)
    {
        getWritableDb().delete(mTable.getName(),selection,arg);
    }

    /**
     * 删除对象
     *
     * @param ob 要删除的对象
     */
    public void delete(T ob)
    {
        if (!checkClass(ob))
            return;
        long id = mUtil.getId(ob);
        if (id < 1)
        {
            Log.e("easydao", "illegal  id");
            return;
        }
        performDeleteById(id);
    }

    public void delete(long id)
    {
        if (id < 1)
        {
            Log.e("EasyDao", "illegal  id");
            return;
        }
        performDeleteById(id);
    }


    /**
     * 删除当前类型的所有数据，不包括数据库文件
     */
    public synchronized void deleteAll()
    {
        SQLiteDatabase db = getWritableDb();
        String sql = "delete from " + mTable.getName();
        SQLiteStatement statement = db.compileStatement(sql);
        statement.execute();
    }

    private synchronized void performDeleteById(long id)
    {

        String sql = "delete from " + mTable.getName() + " where ID = ?";
        SQLiteStatement statement = getWritableDb().compileStatement(sql);
        statement.bindLong(1, id);
        statement.execute();
    }


    private boolean exist(T ob)
    {
        if (!checkClass(ob))
            return false;

        long id = 0;

        String idstr = String.valueOf(id);
        SQLiteDatabase db = getReadableDb();
        SQLiteStatement statement = db.compileStatement("select count(*) from " + mTable.getName() + " where ID=?");
        id = DatabaseUtils.longForQuery(statement, new String[]{idstr});
        return id > 0;
    }


    private void performUpdate(T ob) throws Exception
    {
        SQLiteDatabase db = getWritableDb();
        String sql = UpdateCreator.Update(mTable.getName()).set(mTable.getColumnNames()).where("ID").Build();
        SQLiteStatement statement = db.compileStatement(sql);
        setBindEndWidthID(statement, ob);
        statement.execute();
        SaveForeignTable(ob);
    }

    /**
     * 将已经拥有id的对象插入
     *
     * @param ob 实体
     */
    private void performInsert(T ob) throws Exception
    {
        //将 id 赋值 到实体
        SQLiteDatabase db = getWritableDb();
        String sql = InsertCreator.Create(mTable.getName()).Columns(mTable.getColumnNamesWithID()).Build();
        SQLiteStatement statement = db.compileStatement(sql);
        setBindStartWidthID(statement, ob);
        statement.execute();
        SaveForeignTable(ob);
    }

    /**
     * 将无id的对象插入，并将生成的id赋值到对象
     *
     * @param ob 实体
     */
    private void performInsertNew(T ob, SQLiteStatement statement) throws Exception
    {
        setBind(statement, ob);
        long id = statement.executeInsert();
        mTable.getIdField().set(ob, id);
        SaveForeignTable(ob);
    }

    /**
     * 保存关联表的成员
     *
     * @param ob 对象
     * @throws Exception
     */

    private void SaveForeignTable(Object ob) throws Exception
    {
        //处理外表
        if (!mTable.haveForeign())
        {
            return;
        }
        Map<String, Class> map = mTable.getForeignTables();
        for(Map.Entry<String, Class> entry : map.entrySet())
        {
            String fieldname = entry.getKey();
            EasyDAO dao = EasyDAO.getInstance(entry.getValue());
            if (dao != null)
            {
                Field field = ob.getClass().getDeclaredField(fieldname);
                field.setAccessible(true);
                Object foreign = field.get(ob);
                if (foreign != null)
                {
                    //先保存关联的对象
                    dao.save(foreign);

                    //获取关联对象id
                    long foreignId = dao.getTable().getIdField().getLong(foreign);
                    //获取自己id
                    long id = mUtil.getId(ob);

                    ContentValues cv = new ContentValues();
                    cv.put(entry.getValue().getSimpleName() + "_id", foreignId);

                    //更新外键
                    getWritableDb().update(mTable.getName(), cv, "ID=?", new String[]{String.valueOf(id)});

                } else
                    throw new Exception("getForeing failed");
            } else
                throw new Exception("getDaoInstance failed");
        }
    }

    /**
     * 获取外键值
     *
     * @param type 外键关联表所对应的实体类型
     * @return id
     */
    private long getForeignID(Class type, Object ob)
    {

        long id = mUtil.getId(ob);

        Cursor cursor = getReadableDb().query(mTable.getName(), new String[]{type.getSimpleName() + "_id"}, "ID=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor.moveToNext())
        {
            return cursor.getInt(0);
        }
        cursor.close();
        return -1;
    }


    /**
     * 加载关联外表的成员
     *
     * @param ob 对象
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private void LoadForeignObject(Object ob) throws NoSuchFieldException, IllegalAccessException
    {
        for(Map.Entry<String, Class> entry : mTable.getForeignTables().entrySet())
        {
            //先获取外键id
            long fid = getForeignID(entry.getValue(), ob);
            if (fid < 0)
                continue;
            //通过id找到外表的这一行,通过这一行数据实例化一个成员对象
            Object fieldObject = EasyDAO.getInstance(entry.getValue()).queryById(fid);
            //赋值
            Field field = ob.getClass().getDeclaredField(entry.getKey());
            field.setAccessible(true);
            field.set(ob, fieldObject);
        }
    }


    private void setBind(SQLiteStatement statement, Object ob) throws IOException, IllegalAccessException
    {
        int n = mTable.getColumnNames().length;
        for(int i = 0; i < n; i++)
        {
            bind(statement, i + 1, mTable.getFields().get(i).get(ob));
        }
    }

    private void setBindStartWidthID(SQLiteStatement statement, Object ob) throws IOException, IllegalAccessException
    {
        int n = mTable.getColumnNames().length;
        bind(statement, 1, mTable.getIdField().get(ob));
        for(int i = 0; i < n; i++)
        {
            bind(statement, i + 2, mTable.getFields().get(i).get(ob));
        }
    }

    private void setBindEndWidthID(SQLiteStatement statement, Object ob) throws IOException, IllegalAccessException
    {
        int n = mTable.getColumnNames().length;
        for(int i = 0; i < n; i++)
        {
            bind(statement, i + 1, mTable.getFields().get(i).get(ob));
        }
        bind(statement, n + 1, mTable.getIdField().get(ob));
    }


    private void bind(SQLiteStatement mStatement, int i, Object o) throws IOException
    {

        if (o == null)
        {
            mStatement.bindNull(i);
        } else if (!(o instanceof CharSequence) && !(o instanceof Boolean) && !(o instanceof Character))
        {
            if (!(o instanceof Float) && !(o instanceof Double))
            {
                if (o instanceof Number)
                {
                    mStatement.bindLong(i, ((Number) o).longValue());
                } else if (o instanceof byte[])
                {
                    mStatement.bindBlob(i, (byte[]) o);
                } else
                {
                    mStatement.bindNull(i);
                }
            } else
            {
                mStatement.bindDouble(i, ((Number) o).doubleValue());
            }
        } else
        {
            mStatement.bindString(i, String.valueOf(o));
        }

    }
}
