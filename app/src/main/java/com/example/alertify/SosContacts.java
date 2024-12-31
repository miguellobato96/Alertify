package com.example.alertify;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
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
        updatePinnedContactsUI();
        updateNoContactsMessage();
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
            try (Cursor cursor = getContentResolver().query(data.getData(), new String[]{
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    if (!isContactAlreadyExists(name)) {
                        addContactToDatabase(name, false); // Default unpinned
                        addContactToView(name, false);
                    } else {
                        Toast.makeText(this, "This contact already exists!", Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error retrieving contact details.", Toast.LENGTH_SHORT).show();
            }
            updateNoContactsMessage();
        }
    }

    private void addContactToDatabase(String name, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("isPinned", isPinned ? 1 : 0);
        database.insert("contacts", null, values);
    }

    private boolean isContactAlreadyExists(String name) {
        try (Cursor cursor = database.query("contacts", null, "name=?", new String[]{name}, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    private void addContactToView(String name, boolean isPinned) {
        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setOrientation(LinearLayout.HORIZONTAL);
        contactRow.setPadding(16, 16, 16, 16);
        contactRow.setBackgroundColor(Color.TRANSPARENT);

        ImageView starIcon = new ImageView(this);
        LinearLayout.LayoutParams starParams = new LinearLayout.LayoutParams(100, 100);
        starParams.gravity = Gravity.CENTER_VERTICAL;
        starIcon.setLayoutParams(starParams);
        starIcon.setImageResource(isPinned ? R.drawable.starwbg : R.drawable.starnobg);
        starIcon.setTag(isPinned); // Store pin state
        starIcon.setOnClickListener(v -> togglePinState(starIcon, contactRow, name, isPinned));
        contactRow.addView(starIcon);

        TextView contactText = new TextView(this);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        textParams.gravity = Gravity.CENTER_VERTICAL;
        contactText.setLayoutParams(textParams);
        contactText.setText(name);
        contactText.setTextSize(18);
        contactRow.addView(contactText);

        ImageView dropdownButton = new ImageView(this);
        dropdownButton.setImageResource(R.drawable.t_dots);
        dropdownButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        dropdownButton.setOnClickListener(v -> showDropdownMenu(dropdownButton, contactRow, name));
        contactRow.addView(dropdownButton);

        if (isPinned) {
            pinnedContactListContainer.setVisibility(View.VISIBLE);
            pinnedContactListContainer.addView(contactRow);
        } else {
            contactListContainer.addView(contactRow);
        }

        updatePinnedContactsUI();
        updateNoContactsMessage();
    }

    private void togglePinState(ImageView starIcon, LinearLayout contactRow, String name, boolean isCurrentlyPinned) {
        if (isCurrentlyPinned) {
            // Unpin the contact
            pinnedContactListContainer.removeView(contactRow);
            addContactToView(name, false);
            updateContactInDatabase(name, false);
        } else {
            // Pin the contact
            if (pinnedContactListContainer.getChildCount() >= MAX_PINNED_CONTACTS) {
                Toast.makeText(this, "You can only pin up to " + MAX_PINNED_CONTACTS + " contacts.", Toast.LENGTH_SHORT).show();
                return;
            }
            contactListContainer.removeView(contactRow);
            addContactToView(name, true);
            updateContactInDatabase(name, true);
        }
    }

    private void updateContactInDatabase(String name, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("isPinned", isPinned ? 1 : 0);
        database.update("contacts", values, "name=?", new String[]{name});
    }

    private void showDropdownMenu(View anchor, LinearLayout contactRow, String name) {
        View dropdownMenu = getLayoutInflater().inflate(R.layout.activity_dropdown_layout, null);

        PopupWindow popupWindow = new PopupWindow(
                dropdownMenu,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.dropdown_background));
        popupWindow.setElevation(10);

        TextView deleteButton = dropdownMenu.findViewById(R.id.btn_delete);

        deleteButton.setOnClickListener(v -> {
            deleteContact(contactRow, name);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, -50, 0);
    }

    private void deleteContact(LinearLayout contactRow, String name) {
        database.delete("contacts", "name=?", new String[]{name});
        contactListContainer.removeView(contactRow);
        updatePinnedContactsUI();
        updateNoContactsMessage();
    }

    private void loadContacts() {
        pinnedContactListContainer.removeAllViews();
        contactListContainer.removeAllViews();

        Cursor pinnedCursor = database.query("contacts", null, "isPinned = ?", new String[]{"1"}, null, null, null);
        while (pinnedCursor.moveToNext()) {
            String name = pinnedCursor.getString(pinnedCursor.getColumnIndexOrThrow("name"));
            addContactToView(name, true);
        }
        pinnedCursor.close();

        Cursor unpinnedCursor = database.query("contacts", null, "isPinned = ?", new String[]{"0"}, null, null, null);
        while (unpinnedCursor.moveToNext()) {
            String name = unpinnedCursor.getString(unpinnedCursor.getColumnIndexOrThrow("name"));
            addContactToView(name, false);
        }
        unpinnedCursor.close();

        updatePinnedContactsUI();
        updateNoContactsMessage();
    }

    private void updatePinnedContactsUI() {
        boolean hasPinnedContacts = pinnedContactListContainer.getChildCount() > 0;

        findViewById(R.id.pinned_contacts_gap).setVisibility(hasPinnedContacts ? View.GONE : View.VISIBLE);
    }

    private void updateNoContactsMessage() {
        TextView noContactsMessage = findViewById(R.id.no_contacts_message);
        boolean noContacts = contactListContainer.getChildCount() == 0 && pinnedContactListContainer.getChildCount() == 0;
        noContactsMessage.setVisibility(noContacts ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
