package com.example.jakub.floristsinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.jakub.floristsinventory.data.FlowersContract.FlowersEntry;

/**
 * Created by Jakub on 2017-12-30.
 */

public class FlowersDbHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "florists.db";
    private static final String TEXT_TYPE = " TEXT";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FlowersEntry.TABLE_NAME + " (" +
                    FlowersEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    FlowersEntry.COLUMN_FLOWER_NAME + TEXT_TYPE + " NOT NULL, " +
                    FlowersEntry.COLUMN_FLOWER_TYPE + " INTEGER NOT NULL, " +
                    FlowersEntry.COLUMN_FLOWER_PRICE + " FLOAT NOT NULL, " +
                    FlowersEntry.COLUMN_FLOWER_QUANTITY + " INTEGER DEFAULT 0, " +
                    FlowersEntry.COLUMN_FLOWER_IMAGE + TEXT_TYPE + ");";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FlowersEntry.TABLE_NAME;

    public FlowersDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }
}
