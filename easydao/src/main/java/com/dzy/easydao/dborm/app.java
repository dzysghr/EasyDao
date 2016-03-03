package com.dzy.easydao.dborm;

import android.app.Application;

import com.dzy.easydao.dborm.orm.EasyDAO;

/**
 *
 * Created by dzysg on 2016/3/2 0002.
 */
public class app extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        EasyDAO.attachContext(this);
    }
}
