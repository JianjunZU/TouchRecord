package com.sybilandjoel.jz.touchrecord;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by JZ on 2017/9/6.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {
    final String CREATE_TABLE_SQL =
            "create table locs(_id integer primary key autoincrement , timestamp , x, y, swp)";
    public MyDatabaseHelper(Context context, String name, int version){
        super(context,name,null,version);
    }
    @Override
    public void  onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE_SQL);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){

    }
}
