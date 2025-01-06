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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

import com.example.alertify.database.ContactDatabaseHelper;

public class SosContacts extends AppCompatActivity {

    // Containers for pinned and unpinned contacts
    private LinearLayout pinnedContactListContainer;
    private LinearLayout contactListContainer;
    private SQLiteDatabase database; // Database instance
    private static final int CONTACT_PICKER_REQUEST = 1; // Request code for contact picker
    private static final int MAX_PINNED_CONTACTS = 4; // Maximum number of pinned contacts

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos_contact_list);

        // Initialize the database helper and get a writable database instance
        ContactDatabaseHelper contactDatabaseHelper = new ContactDatabaseHelper(this);
        database = contactDatabaseHelper.getWritableDatabase();

        // Link UI elements
        pinnedContactListContainer = findViewById(R.id.pinned_contact_list_container);
        contactListContainer = findViewById(R.id.contact_list_container);
        Button addContactButton = findViewById(R.id.add_contact_button);

        // Back button functionality
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Set click listener for adding contacts
        addContactButton.setOnClickListener(view -> openContactsPicker());

        // Load contacts from the database and update UI
        loadContacts();
        updateNoContactsMessage();
    }

    // Open the contact picker to select a contact
    private void openContactsPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, CONTACT_PICKER_REQUEST);
    }

    // Handle the result of the contact picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_REQUEST && resultCode == RESULT_OK && data != null) {
            try (Cursor cursor = getContentResolver().query(data.getData(), new String[]{
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (!isContactAlreadyExists(name)) {
                        // Add contact to database and update UI
                        addContactToDatabase(name, number, false);
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

    // Add a contact to the database
    private void addContactToDatabase(String name, String number, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);
        values.put("isPinned", isPinned ? 1 : 0);
        database.insert("contacts", null, values);
    }

    // Check if a contact already exists in the database
    private boolean isContactAlreadyExists(String name) {
        try (Cursor cursor = database.query("contacts", null, "name=?", new String[]{name}, null, null, null)) {
            return cursor.getCount() > 0;
        }
    }

    // Add a contact to the UI
    private void addContactToView(String name, boolean isPinned) {
        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setOrientation(LinearLayout.HORIZONTAL);
        contactRow.setGravity(Gravity.CENTER_VERTICAL);
        contactRow.setPadding(16, 8, 16, 8);
        contactRow.setBackgroundColor(Color.TRANSPARENT);

        // Star icon for pinning/unpinning
        ImageView starIcon = new ImageView(this);
        starIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        starIcon.setImageResource(isPinned ? R.drawable.starwbg : R.drawable.starnobg);
        starIcon.setTag(isPinned);
        starIcon.setOnClickListener(v -> togglePinState(starIcon, contactRow, name));
        contactRow.addView(starIcon);

        // Contact name
        TextView contactText = new TextView(this);
        contactText.setText(name);
        contactText.setTextSize(18);
        contactText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        contactRow.addView(contactText);

        // Dropdown button for edit/delete options
        ImageView dropdownButton = new ImageView(this);
        dropdownButton.setImageResource(R.drawable.t_dots);
        dropdownButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        dropdownButton.setOnClickListener(v -> showDropdownMenu(dropdownButton, contactRow, name, isPinned));
        contactRow.addView(dropdownButton);

        // Add to appropriate container (pinned or unpinned)
        if (isPinned) {
            pinnedContactListContainer.setVisibility(View.VISIBLE);
            pinnedContactListContainer.addView(contactRow);
        } else {
            contactListContainer.addView(contactRow);
        }

        updatePinnedGapVisibility();
    }

    // Toggle the pin state of a contact
    private void togglePinState(ImageView starIcon, LinearLayout contactRow, String name) {
        boolean isCurrentlyPinned = (boolean) starIcon.getTag();
        if (isCurrentlyPinned) {
            pinnedContactListContainer.removeView(contactRow);
            addContactToView(name, false);
            updateContactInDatabase(name, false);
        } else {
            if (pinnedContactListContainer.getChildCount() >= MAX_PINNED_CONTACTS) {
                Toast.makeText(this, "You can only pin up to " + MAX_PINNED_CONTACTS + " contacts.", Toast.LENGTH_SHORT).show();
                return;
            }
            contactListContainer.removeView(contactRow);
            addContactToView(name, true);
            updateContactInDatabase(name, true);
        }
        starIcon.setTag(!isCurrentlyPinned);
    }

    // Update a contact's pin state in the database
    private void updateContactInDatabase(String name, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("isPinned", isPinned ? 1 : 0);
        database.update("contacts", values, "name=?", new String[]{name});
    }

    // Show dropdown menu for edit/delete options
    private void showDropdownMenu(View anchor, LinearLayout contactRow, String name, boolean isPinned) {
        View dropdownMenu = getLayoutInflater().inflate(R.layout.activity_dropdown_layout, null);

        PopupWindow popupWindow = new PopupWindow(
                dropdownMenu,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.dropdown_background));
        popupWindow.setElevation(10);

        TextView editButton = dropdownMenu.findViewById(R.id.btn_edit);
        TextView deleteButton = dropdownMenu.findViewById(R.id.btn_delete);

        editButton.setOnClickListener(v -> {
            enableEditMode(contactRow, name, isPinned);
            popupWindow.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            deleteContact(contactRow, name);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, -50, 0);
    }

    // Enable edit mode for a contact
    private void enableEditMode(LinearLayout contactRow, String oldName, boolean wasPinned) {
        contactRow.removeAllViews();

        EditText editText = new EditText(this);
        editText.setText(oldName);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        contactRow.addView(editText);

        ImageView confirmButton = new ImageView(this);
        confirmButton.setImageResource(android.R.drawable.ic_menu_save);
        confirmButton.setOnClickListener(v -> {
            String newName = editText.getText().toString();
            updateContactInDatabase(oldName, newName, wasPinned);
            loadContacts();
        });
        contactRow.addView(confirmButton);

        ImageView cancelButton = new ImageView(this);
        cancelButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        cancelButton.setOnClickListener(v -> loadContacts());
        contactRow.addView(cancelButton);
    }

    // Update a contact's name in the database
    private void updateContactInDatabase(String oldName, String newName, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("isPinned", isPinned ? 1 : 0);
        database.update("contacts", values, "name=?", new String[]{oldName});
    }

    // Delete a contact from the database and UI
    private void deleteContact(LinearLayout contactRow, String name) {
        database.delete("contacts", "name=?", new String[]{name});
        contactListContainer.removeView(contactRow);
        pinnedContactListContainer.removeView(contactRow);
        updateNoContactsMessage();
        updatePinnedGapVisibility();
    }

    // Load contacts from the database into the UI
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

        updateNoContactsMessage();
        updatePinnedGapVisibility();
    }

    // Update the message displayed when there are no contacts
    private void updateNoContactsMessage() {
        TextView noContactsMessage = findViewById(R.id.no_contacts_message);
        boolean noContacts = contactListContainer.getChildCount() == 0 && pinnedContactListContainer.getChildCount() == 0;
        noContactsMessage.setVisibility(noContacts ? View.VISIBLE : View.GONE);
    }

    // Update the visibility of the gap between pinned and unpinned contacts
    private void updatePinnedGapVisibility() {
        TextView pinnedContactsGap = findViewById(R.id.pinned_contacts_gap);
        boolean hasPinnedContacts = pinnedContactListContainer.getChildCount() > 0;
        pinnedContactsGap.setVisibility(hasPinnedContacts ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        database.close(); // Close the database when the activity is destroyed
        super.onDestroy();
    }
}
