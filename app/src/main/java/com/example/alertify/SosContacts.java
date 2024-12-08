package com.example.alertify;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SosContacts extends AppCompatActivity {

    private LinearLayout contactListContainer;
    private SQLiteDatabase database;
    private static final int CONTACT_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_contact_list);

        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        contactListContainer = findViewById(R.id.contact_list_container);
        Button addContactButton = findViewById(R.id.add_contact_button);

        // Open Contacts App
        addContactButton.setOnClickListener(view -> openContactsPicker());

        loadContacts();
    }

    private void openContactsPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, CONTACT_PICKER_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                String[] projection = {
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER
                };

                try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        // Add contact to the database and display in the app
                        addContactToDatabase(name, number);
                        addContactToView(name, number);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error retrieving contact details.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void addContactToDatabase(String name, String number) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);
        database.insert("contacts", null, values);
    }

    private void addContactToView(String name, String number) {
        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setOrientation(LinearLayout.HORIZONTAL);
        contactRow.setPadding(16, 16, 16, 16);
        contactRow.setBackgroundColor(Color.TRANSPARENT);

        // Contact icon
        ImageView contactIcon = new ImageView(this);
        contactIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        contactIcon.setImageResource(R.drawable.ic_person); // Default icon
        contactRow.addView(contactIcon);

        // Contact name and number
        TextView contactText = new TextView(this);
        contactText.setText(name + "\n" + number);
        contactText.setTextSize(18);
        contactText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        contactRow.addView(contactText);

        // Dropdown button (3 dots)
        ImageView dropdownButton = new ImageView(this);
        dropdownButton.setImageResource(R.drawable.t_dots); // Replace with your 3-dots icon
        dropdownButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        contactRow.addView(dropdownButton);

        contactListContainer.addView(contactRow);

        // Set up the dropdown menu
        dropdownButton.setOnClickListener(v -> {
            showDropdownMenu(dropdownButton, contactRow, name, number);
        });
    }

    private void showDropdownMenu(View anchor, LinearLayout contactRow, String name, String number) {
        // Inflate dropdown menu layout
        View dropdownMenu = getLayoutInflater().inflate(R.layout.activity_dropdown_layout, null);

        // Create PopupWindow
        PopupWindow popupWindow = new PopupWindow(
                dropdownMenu,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Background and elevation for the dropdown
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.dropdown_background));
        popupWindow.setElevation(10);

        // Handle dropdown actions
        TextView pinButton = dropdownMenu.findViewById(R.id.btn_pin);
        TextView editButton = dropdownMenu.findViewById(R.id.btn_edit);
        TextView deleteButton = dropdownMenu.findViewById(R.id.btn_delete);

        pinButton.setOnClickListener(v -> {
            Toast.makeText(this, "Pin clicked for " + name, Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        editButton.setOnClickListener(v -> {
            Toast.makeText(this, "Edit clicked for " + name, Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            deleteContact(contactRow, name, number);
            popupWindow.dismiss();
        });

        // Show the popup aligned to the dropdown button
        popupWindow.showAsDropDown(anchor, -50, 0);
    }

    private void deleteContact(LinearLayout contactRow, String name, String number) {
        database.delete("contacts", "name=? AND number=?", new String[]{name, number});
        contactListContainer.removeView(contactRow);
    }

    private void loadContacts() {
        Cursor cursor = database.query("contacts", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            String number = cursor.getString(cursor.getColumnIndexOrThrow("number"));
            addContactToView(name, number);
        }
        cursor.close();
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
