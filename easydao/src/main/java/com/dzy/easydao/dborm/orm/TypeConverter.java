package com.dzy.easydao.dborm.orm;

/**
 *
 * Created by dzysg on 2016/2/25 0025.
 */
public class TypeConverter
{
    public static String getTypeSTring(Class<?> type)
    {
                switch (type.getSimpleName())
                {
                    case "int":
                    case "Integer":
                    case "long":
                    case "Long":
                    case "short":
                    case "Short":
                        return "Integer";
                    case "String":
                    case "Character":
                    case "char":
                    case "boolean":
                    case "Boolean":
                        return "TEXT";
                    case "float":
                    case "double":
                   return "REAL";
                    case "byte[]":
                        return "Blob";


                    default:
                        throw new IllegalArgumentException(type.toString()+" is not supported");
                }
    }

}
