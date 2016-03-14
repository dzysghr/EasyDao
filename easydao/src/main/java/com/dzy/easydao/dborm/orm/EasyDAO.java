package com.dzy.easydao.dborm.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.dzy.easydao.dborm.SqlGenerate.InsertCreator;
import com.dzy.easydao.dborm.SqlGenerate.UpdateCreator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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

    public static void attachContext(Context c)
    {
        mContext = c;
    }

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
        mHelper = DBHelper.getInstance(mContext);
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
            mWriteDb.close();
        if (mReadDb != null && mReadDb.isOpen())
            mReadDb.close();
    }


    private SQLiteDatabase getWritableDb()
    {
        if (mWriteDb == null)
            mWriteDb = mHelper.getWritableDatabase();
        return mWriteDb;
    }

    private SQLiteDatabase getReadableDb()
    {
        if (mReadDb == null)
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


    public T qureyFirst(String selection, String[] arg)
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
     *
     * @param selection 查询条件 ，如name=?
     * @param arg       查询参数
     * @return list<T>
     */
    public List<T> qureyWhere(String selection, String... arg)
    {
        return qureyWhere(selection, arg, null, null);
    }


    public T qureyById(long id)
    {
        return qureyFirst("ID=?", new String[]{String.valueOf(id)});
    }


    public List<T> qureyWhere(String selection, String[] arg, String orderby, String limit)
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
            statement.acquireReference();
            Iterator<T> in = list.iterator();
            while (in.hasNext())
            {
                statement.clearBindings();
                T ob = in.next();
                performInsertNew(ob, statement);
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
            statement.close();
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


    public void save(Collection<T> list)
    {
        getWritableDb().beginTransaction();
        for(T item:list)
        {
            save(item);
        }
        getWritableDb().setTransactionSuccessful();
        getWritableDb().endTransaction();
    }


    /**
     * 若对象 id存在,则更新数据，否则插入新行
     *
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
            Log.e("easydao", "illegal  id");
            return;
        }
        performDeleteById(id);
    }

    public synchronized void deleteAll()
    {
        SQLiteDatabase db = getWritableDb();
        String sql = "delete from " + mTable.getName();
        SQLiteStatement statement = db.compileStatement(sql);
        statement.execute();
    }

    public synchronized void performDeleteById(long id)
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

        long id = mUtil.getId(ob);
        if (id < 1)
            return false;

        String idstr = String.valueOf(id);
        SQLiteDatabase db = getReadableDb();
        Cursor cursor = db.rawQuery("select * from " + mTable.getName() + " where ID=?", new String[]{idstr});
        if (cursor.moveToNext())
        {
            cursor.close();
            return true;
        } else
        {
            cursor.close();
            return false;
        }
    }


    private synchronized void performUpdate(T ob) throws Exception
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
    private synchronized void performInsert(T ob) throws Exception
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
    private synchronized void performInsertNew(T ob, SQLiteStatement statement) throws Exception
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
        Iterator<Map.Entry<String, Class>> in = map.entrySet().iterator();
        while (in.hasNext())
        {
            Map.Entry<String, Class> entry = in.next();
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
            Object fieldObject = EasyDAO.getInstance(entry.getValue()).qureyById(fid);
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
