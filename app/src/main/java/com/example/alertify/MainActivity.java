package com.example.alertify;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int CONTACT_PICKER_REQUEST_CODE = 100;
    private LinearLayout contactListContainer;
    private SQLiteDatabase database;
    private LinearLayout expandedDropdown = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos_contact_list);

        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        contactListContainer = findViewById(R.id.contact_list_container);
        Button addContactButton = findViewById(R.id.add_contact_button);
        styleAddContactButton(addContactButton);

        addContactButton.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
            } else {
                pickContact();
            }
        });

        loadContacts();
    }

    private void styleAddContactButton(Button button) {
        button.setText("+");
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.parseColor("#7f4aa4"));
        button.setPadding(50, 20, 50, 20);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
        button.setGravity(Gravity.CENTER);
        button.setBackgroundResource(R.drawable.add_contact);
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                String contactId = getContactId(contactUri);
                String contactName = fetchContactName(contactId);
                String contactNumber = fetchPhoneNumber(contactId);

                if (contactName != null && contactNumber != null) {
                    addContactToDatabase(contactName, contactNumber);
                    addContactToView(contactName, contactNumber);
                    Toast.makeText(this, "Contact added successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to retrieve contact details.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String getContactId(Uri contactUri) {
        String contactId = null;
        Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                }
            } finally {
                cursor.close();
            }
        }
        return contactId;
    }

    private String fetchContactName(String contactId) {
        String contactName = null;
        Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                new String[]{ContactsContract.Contacts.DISPLAY_NAME},
                ContactsContract.Contacts._ID + " = ?",
                new String[]{contactId},
                null
        );
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        return contactName;
    }

    private String fetchPhoneNumber(String contactId) {
        String phoneNumber = null;
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{contactId},
                null
        );
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
            } finally {
                cursor.close();
            }
        }
        return phoneNumber;
    }

    private void addContactToDatabase(String name, String number) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);
        database.insert("contacts", null, values);
    }

    private void addContactToView(String name, String number) {
        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setOrientation(LinearLayout.VERTICAL);
        contactRow.setPadding(16, 16, 16, 16);

        LinearLayout mainRow = new LinearLayout(this);
        mainRow.setOrientation(LinearLayout.HORIZONTAL);

        ImageView contactIcon = new ImageView(this);
        contactIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        contactIcon.setImageResource(R.drawable.ic_person);
        mainRow.addView(contactIcon);

        TextView contactText = new TextView(this);
        contactText.setText(name + "\n" + number);
        contactText.setTextSize(18);
        contactText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        mainRow.addView(contactText);

        ImageView dropdownButton = new ImageView(this);
        dropdownButton.setImageResource(android.R.drawable.ic_menu_more);
        dropdownButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        mainRow.addView(dropdownButton);

        contactRow.addView(mainRow);

        LinearLayout dropdownMenu = new LinearLayout(this);
        dropdownMenu.setOrientation(LinearLayout.VERTICAL);
        dropdownMenu.setBackgroundColor(Color.parseColor("#AA7F4AA4"));
        dropdownMenu.setPadding(20, 20, 20, 20);
        dropdownMenu.setVisibility(View.GONE);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(5, 10, 5, 10);

        Button pinButton = new Button(this);
        pinButton.setText("Pin/Star");
        pinButton.setLayoutParams(buttonParams);
        pinButton.setOnClickListener(v -> pinContact(contactRow));
        dropdownMenu.addView(pinButton);

        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setLayoutParams(buttonParams);
        editButton.setOnClickListener(v -> showEditContactDialog(contactRow, name, number));
        dropdownMenu.addView(editButton);

        Button deleteButton = new Button(this);
        deleteButton.setText("Trash");
        deleteButton.setLayoutParams(buttonParams);
        deleteButton.setOnClickListener(v -> deleteContact(contactRow, name, number));
        dropdownMenu.addView(deleteButton);

        contactRow.addView(dropdownMenu);

        dropdownButton.setOnClickListener(v -> {
            if (expandedDropdown != null && expandedDropdown != dropdownMenu) {
                expandedDropdown.setVisibility(View.GONE);
            }

            if (dropdownMenu.getVisibility() == View.GONE) {
                dropdownMenu.setVisibility(View.VISIBLE);
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(dropdownMenu, "alpha", 0f, 1f);
                fadeIn.setDuration(300);
                fadeIn.setInterpolator(new AccelerateDecelerateInterpolator());
                fadeIn.start();

                contactRow.setBackgroundColor(Color.parseColor("#D8BED8"));
                expandedDropdown = dropdownMenu;
            } else {
                dropdownMenu.setVisibility(View.GONE);
                contactRow.setBackgroundColor(Color.TRANSPARENT);
                expandedDropdown = null;
            }
        });

        contactListContainer.addView(contactRow);
    }

    private void deleteContact(LinearLayout contactRow, String name, String number) {
        database.delete("contacts", "name=? AND number=?", new String[]{name, number});
        contactListContainer.removeView(contactRow);
    }

    private void pinContact(LinearLayout contactRow) {
        Toast.makeText(this, "Pinned contact feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showEditContactDialog(LinearLayout contactRow, String name, String number) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Contact");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        EditText nameInput = new EditText(this);
        nameInput.setText(name);
        layout.addView(nameInput);

        EditText numberInput = new EditText(this);
        numberInput.setText(number);
        numberInput.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(numberInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            String newNumber = numberInput.getText().toString().trim();

            if (!newName.isEmpty() && !newNumber.isEmpty() && newNumber.length() == 9) {
                updateContactInDatabase(name, number, newName, newNumber);
                ((TextView) ((LinearLayout) contactRow.getChildAt(0)).getChildAt(1))
                        .setText(newName + "\n" + newNumber);
            } else {
                Toast.makeText(this, "Both fields are required, and the phone number must be 9 digits.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void updateContactInDatabase(String oldName, String oldNumber, String newName, String newNumber) {
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("number", newNumber);
        database.update("contacts", values, "name=? AND number=?", new String[]{oldName, oldNumber});
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
