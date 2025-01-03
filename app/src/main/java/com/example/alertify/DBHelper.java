package com.example.alertify;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alertify.db";
    private static final int DATABASE_VERSION = 3; // Increment the version

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contacts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "number TEXT, " +
                "isPinned INTEGER DEFAULT 0, " +
                "pinned_order INTEGER DEFAULT NULL" +
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE contacts ADD COLUMN isPinned INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE contacts ADD COLUMN pinned_order INTEGER DEFAULT NULL");
        }
    }
}

