package com.dzy.easydao.dborm.orm;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.List;

/**
 *
 * Created by dzysg on 2016/2/29 0029.
 */
public class Util<T>
{

    public TableInfo mTableInfo;
    public Class mType;


    public Util(TableInfo tableInfo, Class t)
    {
        mTableInfo = tableInfo;
        mType = t;
    }



    /** 生成对象数据的ContentValue
     * @param ob 对象
     * @param withId 是否包括id列
     * @return ContentValue
     * @throws IllegalAccessException
     */
    public ContentValues CreateContentValue(T ob, boolean withId) throws IllegalAccessException
    {
        ContentValues cv = new ContentValues();
        List<Field> fields = mTableInfo.getFields();
        String[] names = mTableInfo.getColumnNames();


            if (withId) {
                cv.put("ID", (long) mTableInfo.getIdField().get(ob));
            }
            for (int i = 0; i < fields.size(); i++) {
                Class type = fields.get(i).getType();
                String cname = names[i];

                if (type == int.class || type == Integer.class)
                    cv.put(cname, (int) fields.get(i).get(ob));
                else if (type == long.class)
                    cv.put(cname, (long) fields.get(i).get(ob));
                else if (type == short.class)
                    cv.put(cname, (short) fields.get(i).get(ob));
                else if (type == String.class)
                    cv.put(cname, (String) fields.get(i).get(ob));
                else if (type == byte.class)
                    cv.put(cname, (byte) fields.get(i).get(ob));
                else if (type == float.class)
                    cv.put(cname, (float) fields.get(i).get(ob));
                else if (type == double.class)
                    cv.put(cname, (double) fields.get(i).get(ob));
                else if (type == Boolean.class)
                    cv.put(cname, (Boolean) fields.get(i).get(ob));
                else if (type == byte[].class)
                    cv.put(cname, (byte[]) fields.get(i).get(ob));

            }
            return cv;
    }


    /** 获取对象的id
     * @param ob 对象
     * @return id
     */
    public long getId(Object ob)
    {
        long id = -1;
        try {
            id = (long) mTableInfo.getIdField().get(ob);
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
        return id;
    }


    /** 获取一个的新对象实例
     * @return 实例
     */
    public T NewInstance()
    {
        //SugarRecord
        try {

            return (T)mType.getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
            Log.e("tag", "the object should have a  Constructor without params");
            Log.e("tag", e.getMessage());
        }
        return null;
    }


    /** 从cursor 读取各属性值并实例化对象
     * @param c  cursor
     * @return 查询结果的对象
     * @throws IllegalAccessException
     */
    public  T  LoadInstance(Cursor c) throws IllegalAccessException
    {
        T t  = this.NewInstance();
        List<Field> fields = mTableInfo.getFields();
        String[] cnames = mTableInfo.getColumnNames();


        mTableInfo.getIdField().set(t, c.getLong(0));
        for (int i = 0; i < cnames.length; i++)
        {
            String typeName = fields.get(i).getType().getSimpleName();
            switch (typeName) {
                case "int":
                case "Integer":
                    fields.get(i).set(t, c.getInt(c.getColumnIndex(cnames[i])));
                    break;
                case "long":
                case "Long":
                    fields.get(i).set(t, c.getLong(c.getColumnIndex(cnames[i])));
                    break;
                case "short":
                case "Short":
                    fields.get(i).set(t, c.getShort(c.getColumnIndex(cnames[i])));
                    break;
                case "float":
                case "Float":
                    fields.get(i).set(t, c.getFloat(c.getColumnIndex(cnames[i])));
                    break;
                case "double":
                case "Double":
                    fields.get(i).set(t, c.getDouble(c.getColumnIndex(cnames[i])));
                    break;
                case "String":
                    fields.get(i).set(t, c.getString(c.getColumnIndex(cnames[i])));
                    break;
                case "byte[]":
                    fields.get(i).set(t, c.getBlob(c.getColumnIndex(cnames[i])));
                    break;
            }
        }
        return t;
    }
}
