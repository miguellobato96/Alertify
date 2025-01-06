package com.example.alertify.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    // Database information
    private static final String DATABASE_NAME = "UserDatabase"; // Name of the database
    private static final int DATABASE_VERSION = 1; // Fixed to 1 since versioning is not needed

    // Table and columns
    private static final String TABLE_USERS = "users"; // Users table name
    private static final String COLUMN_ID = "id"; // User ID column
    private static final String COLUMN_FULL_NAME = "full_name"; // Full name column
    private static final String COLUMN_EMAIL = "email"; // Email column
    private static final String COLUMN_PASSWORD = "password"; // Password column

    // Create table SQL query
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + // Auto-incrementing ID
                    COLUMN_FULL_NAME + " TEXT, " +                       // Full name field
                    COLUMN_EMAIL + " TEXT UNIQUE, " +                    // Unique email field
                    COLUMN_PASSWORD + " TEXT);";                         // Password field

    // Constructor
    public UserDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create table when the database is first created
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE); // Execute SQL query to create the users table
    }

    // Upgrade method no longer needed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Empty implementation since versioning is not required
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the table and recreate it to handle database downgrades
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }


    // Method to insert a new user into the database
    public boolean addUser(String fullName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values); // Insert the new user
        db.close();

        return result != -1; // Return true if insertion was successful
    }

    // Method to check if a user exists in the database
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = {email, password}; // Bind email and password

        Cursor cursor = db.rawQuery(query, selectionArgs); // Execute the query
        boolean exists = cursor.getCount() > 0; // Check if any results were returned

        cursor.close(); // Close the cursor
        db.close(); // Close the database connection

        return exists; // Return true if the user exists
    }
}
