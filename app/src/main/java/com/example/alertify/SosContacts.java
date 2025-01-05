package com.example.alertify;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
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
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (!isContactAlreadyExists(name)) {
                        addContactToDatabase(name, number, false); // Inclui o número do contato
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

    private void addContactToDatabase(String name, String number, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number); // Salva o número do contato
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
        starIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        starIcon.setImageResource(isPinned ? R.drawable.starwbg : R.drawable.starnobg);
        starIcon.setTag(isPinned); // Store pin state
        starIcon.setOnClickListener(v -> togglePinState(starIcon, contactRow, name));
        contactRow.addView(starIcon);

        TextView contactText = new TextView(this);
        contactText.setText(name);
        contactText.setTextSize(18);
        contactText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        contactRow.addView(contactText);

        ImageView dropdownButton = new ImageView(this);
        dropdownButton.setImageResource(R.drawable.t_dots);
        dropdownButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        dropdownButton.setOnClickListener(v -> showDropdownMenu(dropdownButton, contactRow, name, isPinned));
        contactRow.addView(dropdownButton);

        if (isPinned) {
            pinnedContactListContainer.setVisibility(View.VISIBLE);
            pinnedContactListContainer.addView(contactRow);
        } else {
            contactListContainer.addView(contactRow);
        }

        updatePinnedGapVisibility();
    }

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

    private void updateContactInDatabase(String name, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("isPinned", isPinned ? 1 : 0);
        database.update("contacts", values, "name=?", new String[]{name});
    }

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

        // TextView editButton = dropdownMenu.findViewById(R.id.btn_edit);
        TextView deleteButton = dropdownMenu.findViewById(R.id.btn_delete);

        editButton.setOnClickListener(v -> {
            enableEditMode(contactRow, name, isPinned);
            popupWindow.dismiss();
        });
      //  editButton.setOnClickListener(v -> {
      //      enableEditMode(contactRow, name, isPinned);
      //      popupWindow.dismiss();
      //  });

        deleteButton.setOnClickListener(v -> {
            deleteContact(contactRow, name);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, -50, 0);
    }

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

    private void updateContactInDatabase(String oldName, String newName, boolean isPinned) {
        ContentValues values = new ContentValues();
        values.put("name", newName);
        values.put("isPinned", isPinned ? 1 : 0);
        database.update("contacts", values, "name=?", new String[]{oldName});
    }

    private void deleteContact(LinearLayout contactRow, String name) {
        database.delete("contacts", "name=?", new String[]{name});
        contactListContainer.removeView(contactRow);
        pinnedContactListContainer.removeView(contactRow);
        updateNoContactsMessage();
        updatePinnedGapVisibility();
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

        Cursor unpinnedCursor = database.query("contacts", null, "isPinned = ?", new String[]{"0"}, null, null,null);
        while (unpinnedCursor.moveToNext()) {
            String name = unpinnedCursor.getString(unpinnedCursor.getColumnIndexOrThrow("name"));
            addContactToView(name, false);
        }
        unpinnedCursor.close();

        updateNoContactsMessage();
        updatePinnedGapVisibility();
    }

    private void updateNoContactsMessage() {
        TextView noContactsMessage = findViewById(R.id.no_contacts_message);
        boolean noContacts = contactListContainer.getChildCount() == 0 && pinnedContactListContainer.getChildCount() == 0;
        noContactsMessage.setVisibility(noContacts ? View.VISIBLE : View.GONE);
    }

    private void updatePinnedGapVisibility() {
        TextView pinnedContactsGap = findViewById(R.id.pinned_contacts_gap);
        boolean hasPinnedContacts = pinnedContactListContainer.getChildCount() > 0;
        pinnedContactsGap.setVisibility(hasPinnedContacts ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
