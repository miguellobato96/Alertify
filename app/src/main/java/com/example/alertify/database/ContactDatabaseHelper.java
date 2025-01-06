package com.example.alertify.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "alertify.db"; // Database name
    private static final int DATABASE_VERSION = 1; // Fixed to 1 since no versioning is needed

    public ContactDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement to create the "contacts" table
        db.execSQL("CREATE TABLE contacts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " + // Unique ID for each contact
                "name TEXT, " +                            // Name of the contact
                "number TEXT, " +                          // Phone number of the contact
                "isPinned INTEGER DEFAULT 0, " +           // Whether the contact is pinned (0 = not pinned, 1 = pinned)
                "pinned_order INTEGER DEFAULT NULL" +      // Order for pinned contacts
                ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This method is now empty because database versioning is not needed
    }
}
