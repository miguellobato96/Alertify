package com.example.alertify;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout contactListContainer;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos_contact_list);

        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        contactListContainer = findViewById(R.id.contact_list_container);
        Button addContactButton = findViewById(R.id.add_contact_button);
        addContactButton.setOnClickListener(view -> showAddContactDialog());

        loadContacts();
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Contact");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 40);

        EditText nameInput = new EditText(this);
        nameInput.setHint("Name");
        nameInput.setTextSize(20);
        nameInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        nameInput.setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                char currentChar = source.charAt(i);
                if (!Character.isLetter(currentChar) && !Character.isSpaceChar(currentChar)) {
                    return ""; // Reject invalid characters
                }
            }
            return null; // Accept valid input
        }});
        layout.addView(nameInput);

        EditText numberInput = new EditText(this);
        numberInput.setHint("Phone Number");
        numberInput.setTextSize(20);
        numberInput.setInputType(InputType.TYPE_CLASS_PHONE);
        numberInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        layout.addView(numberInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String number = numberInput.getText().toString().trim();

            if (!name.isEmpty() && !number.isEmpty() && number.length() == 9) {
                addContactToDatabase(name, number);
                addContactToView(name, number);
            } else {
                Toast.makeText(this, "Both fields are required, and the phone number must be 9 digits.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
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
        contactIcon.setImageResource(R.drawable.ic_person);
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
        View dropdownMenu = getLayoutInflater().inflate(R.layout.dropdown_layout, null);

        // Create PopupWindow
        PopupWindow popupWindow = new PopupWindow(dropdownMenu,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        // Background and elevation for the dropdown
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.dropdown_background));
        popupWindow.setElevation(10);

        // Handle dropdown actions
        dropdownMenu.findViewById(R.id.btn_pin).setOnClickListener(v -> {
            Toast.makeText(this, "Pin clicked for " + name, Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        dropdownMenu.findViewById(R.id.btn_edit).setOnClickListener(v -> {
            showEditContactDialog(contactRow, name, number);
            popupWindow.dismiss();
        });

        dropdownMenu.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            deleteContact(contactRow, name, number);
            popupWindow.dismiss();
        });

        // Show the popup aligned to the dropdown button
        popupWindow.showAsDropDown(anchor, -50, 0);
    }


    private void showPopupDropdown(View anchor, String name, String number) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dropdownView = inflater.inflate(R.layout.dropdown_layout, null);

        PopupWindow popupWindow = new PopupWindow(dropdownView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setElevation(10);

        Button pinButton = dropdownView.findViewById(R.id.btn_pin);
        Button editButton = dropdownView.findViewById(R.id.btn_edit);
        Button deleteButton = dropdownView.findViewById(R.id.btn_delete);

        pinButton.setOnClickListener(v -> {
            Toast.makeText(this, "Pin feature coming soon!", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        editButton.setOnClickListener(v -> {
            showEditContactDialog(anchor, name, number);
            popupWindow.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            deleteContact((LinearLayout) anchor, name, number);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, -50, 0, Gravity.END);
    }

    private void deleteContact(LinearLayout contactRow, String name, String number) {
        database.delete("contacts", "name=? AND number=?", new String[]{name, number});
        contactListContainer.removeView(contactRow);
    }

    private void showEditContactDialog(View anchor, String name, String number) {
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
                ((TextView) ((LinearLayout) anchor).getChildAt(1))
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
