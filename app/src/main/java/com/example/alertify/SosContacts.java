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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SosContacts extends AppCompatActivity {

    private LinearLayout pinnedContactListContainer;
    private LinearLayout contactListContainer;
    private SQLiteDatabase database;
    private static final int CONTACT_PICKER_REQUEST = 1;
    private static final int MAX_PINNED_CONTACTS = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_contact_list);

        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        pinnedContactListContainer = findViewById(R.id.pinned_contact_list_container);
        contactListContainer = findViewById(R.id.contact_list_container);
        Button addContactButton = findViewById(R.id.add_contact_button);

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

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

                        if (!isContactAlreadyExists(name, number)) {
                            addContactToDatabase(name, number, false);
                            addContactToView(name, number, false);
                        } else {
                            Toast.makeText(this, "This contact already exists!", Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error retrieving contact details.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void addContactToDatabase(String name, String number, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);
        values.put("isPinned", isPinned ? 1 : 0);

        if (isPinned) {
            int nextOrder = getNextPinnedOrder();
            values.put("pinned_order", nextOrder);
        } else {
            values.putNull("pinned_order");
        }

        database.insert("contacts", null, values);
    }

    private boolean isContactAlreadyExists(String name, String number) {
        Cursor cursor = database.query(
                "contacts",
                null,
                "name=? AND number=?",
                new String[]{name, number},
                null,
                null,
                null
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void addContactToView(String name, String number, boolean isPinned) {
        if (isPinned && pinnedContactListContainer.getChildCount() >= MAX_PINNED_CONTACTS) {
            Toast.makeText(this, "You can only pin up to " + MAX_PINNED_CONTACTS + " contacts.", Toast.LENGTH_SHORT).show();
            return;
        }

        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setOrientation(LinearLayout.HORIZONTAL);
        contactRow.setPadding(16, 16, 16, 16);
        contactRow.setBackgroundColor(Color.TRANSPARENT);

        ImageView contactIcon = new ImageView(this);
        contactIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        contactIcon.setImageResource(R.drawable.ic_person);
        contactRow.addView(contactIcon);

        TextView contactText = new TextView(this);
        contactText.setText(name + "\n" + number);
        contactText.setTextSize(18);
        contactText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        contactRow.addView(contactText);

        ImageView dropdownButton = new ImageView(this);
        dropdownButton.setImageResource(R.drawable.t_dots);
        dropdownButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        contactRow.addView(dropdownButton);

        if (isPinned) {
            pinnedContactListContainer.setVisibility(View.VISIBLE);
            pinnedContactListContainer.addView(contactRow);
        } else {
            contactListContainer.addView(contactRow);
        }

        dropdownButton.setOnClickListener(v -> showDropdownMenu(dropdownButton, contactRow, name, number, isPinned));
    }

    private void showDropdownMenu(View anchor, LinearLayout contactRow, String name, String number, boolean isPinned) {
        View dropdownMenu = getLayoutInflater().inflate(R.layout.activity_dropdown_layout, null);

        PopupWindow popupWindow = new PopupWindow(
                dropdownMenu,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.dropdown_background));
        popupWindow.setElevation(10);

        TextView pinButton = dropdownMenu.findViewById(R.id.btn_pin);
        TextView editButton = dropdownMenu.findViewById(R.id.btn_edit);
        TextView deleteButton = dropdownMenu.findViewById(R.id.btn_delete);

        pinButton.setText(isPinned ? "Unpin" : "Pin");
        pinButton.setOnClickListener(v -> {
            if (isPinned) {
                pinnedContactListContainer.removeView(contactRow);
                addContactToView(name, number, false);
                updateContactInDatabase(name, number, false);
            } else {
                if (pinnedContactListContainer.getChildCount() >= MAX_PINNED_CONTACTS) {
                    Toast.makeText(this, "You can only pin up to " + MAX_PINNED_CONTACTS + " contacts.", Toast.LENGTH_SHORT).show();
                } else {
                    contactListContainer.removeView(contactRow);
                    addContactToView(name, number, true);
                    updateContactInDatabase(name, number, true);
                }
            }
            popupWindow.dismiss();
        });

        editButton.setOnClickListener(v -> {
            Toast.makeText(this, "Edit clicked for " + name, Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            deleteContact(contactRow, name, number, isPinned);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, -50, 0);
    }

    private void deleteContact(LinearLayout contactRow, String name, String number, boolean isPinned) {
        database.delete("contacts", "name=? AND number=?", new String[]{name, number});
        if (isPinned) {
            pinnedContactListContainer.removeView(contactRow);
        } else {
            contactListContainer.removeView(contactRow);
        }
    }

    private void loadContacts() {
        pinnedContactListContainer.removeAllViews();
        contactListContainer.removeAllViews();

        Cursor pinnedCursor = database.query(
                "contacts",
                null,
                "isPinned = ?",
                new String[]{"1"},
                null,
                null,
                "pinned_order ASC"
        );

        while (pinnedCursor.moveToNext()) {
            String name = pinnedCursor.getString(pinnedCursor.getColumnIndexOrThrow("name"));
            String number = pinnedCursor.getString(pinnedCursor.getColumnIndexOrThrow("number"));
            addContactToView(name, number, true);
        }
        pinnedCursor.close();

        Cursor unpinnedCursor = database.query(
                "contacts",
                null,
                "isPinned = ?",
                new String[]{"0"},
                null,
                null,
                "id ASC"
        );

        while (unpinnedCursor.moveToNext()) {
            String name = unpinnedCursor.getString(unpinnedCursor.getColumnIndexOrThrow("name"));
            String number = unpinnedCursor.getString(unpinnedCursor.getColumnIndexOrThrow("number"));
            addContactToView(name, number, false);
        }
        unpinnedCursor.close();
    }

    private void updateContactInDatabase(String name, String number, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("isPinned", isPinned ? 1 : 0);

        if (isPinned) {
            int nextOrder = getNextPinnedOrder();
            values.put("pinned_order", nextOrder);
        } else {
            values.putNull("pinned_order");
        }

        database.update("contacts", values, "name=? AND number=?", new String[]{name, number});
    }

    private int getNextPinnedOrder() {
        Cursor cursor = database.rawQuery("SELECT MAX(pinned_order) FROM contacts WHERE isPinned = 1", null);
        int nextOrder = 1;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            nextOrder = cursor.getInt(0) + 1;
        }
        cursor.close();
        return nextOrder;
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
