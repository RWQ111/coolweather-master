package com.bogaara.coolweather.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class MyDataBase {

    Context context;
    MyOpenHelper myHelper;
    SQLiteDatabase myDatabase;

    public MyDataBase(Context con){//创建数据库/
        this.context=con;
        myHelper=new MyOpenHelper(context);
    }
    /*
     * 得到ListView的数据，从数据库里查找后解析
     */
    public ArrayList<County> getArray(){
        ArrayList<County> array = new ArrayList<County>();
        ArrayList<County> array1 = new ArrayList<County>();
        myDatabase = myHelper.getWritableDatabase();
        Cursor cursor=myDatabase.rawQuery("select ids,countyName,cityId,weatherId from myWeatherss" , null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            int id = cursor.getInt(cursor.getColumnIndex("ids"));
            String countyName = cursor.getString(cursor.getColumnIndex("countyName"));
            int cityId = cursor.getInt(cursor.getColumnIndex("cityId"));
            String weatherId = cursor.getString(cursor.getColumnIndex("weatherId"));
            County county = new County(id,countyName,cityId,weatherId);
            array.add(county);
            cursor.moveToNext();
        }
        myDatabase.close();
        for (int i = array.size(); i >0; i--) {
            array1.add(array.get(i-1));
        }
        return array1;
    }

    /*
     * 用来增加新的日记
     */
    public void toInsert(County county){
        myDatabase = myHelper.getWritableDatabase();
        myDatabase.execSQL("insert into myWeatherss(countyName,cityId,weatherId)values('"
                +county.getCountyName()+"','"
                +county.getCityId()+"','"
                +county.getWeatherId()+"')");
        myDatabase.close();
    }

    /*
     * 长按点击后选择删除日记
     */
    public void toDelete(int id){
        myDatabase  = myHelper.getWritableDatabase();
        myDatabase.execSQL("delete from myWeatherss where ids="+id+"");
        myDatabase.close();
    }
}
