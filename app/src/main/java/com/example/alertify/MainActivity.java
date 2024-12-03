package com.example.alertify;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout contactListContainer;
    private SQLiteDatabase database;

    private LinearLayout lastOpenedDropdown;
    private View lastOpenedContactRow;

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
        contactRow.setOrientation(LinearLayout.VERTICAL);
        contactRow.setPadding(16, 16, 16, 16);
        contactRow.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout mainRow = new LinearLayout(this);
        mainRow.setOrientation(LinearLayout.HORIZONTAL);

        ImageView contactIcon = new ImageView(this);
        contactIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        contactIcon.setImageResource(R.drawable.ic_person);
        mainRow.addView(contactIcon);

        TextView contactText = new TextView(this);
        contactText.setText("Name: " + name + "\nNumber: " + number);
        contactText.setTextSize(20);
        contactText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        mainRow.addView(contactText);

        contactRow.addView(mainRow);

        LinearLayout dropdownMenu = new LinearLayout(this);
        dropdownMenu.setOrientation(LinearLayout.HORIZONTAL);
        dropdownMenu.setGravity(Gravity.CENTER_HORIZONTAL);
        dropdownMenu.setVisibility(View.GONE);

        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(150, 150);
        iconParams.setMargins(16, 16, 16, 16);

        ImageView deleteIcon = new ImageView(this);
        deleteIcon.setImageResource(android.R.drawable.ic_menu_delete);
        deleteIcon.setLayoutParams(iconParams);
        deleteIcon.setOnClickListener(v -> deleteContact(contactRow, name, number));
        dropdownMenu.addView(deleteIcon);

        ImageView pinIcon = new ImageView(this);
        pinIcon.setImageResource(android.R.drawable.ic_menu_myplaces);
        pinIcon.setLayoutParams(iconParams);
        pinIcon.setOnClickListener(v -> pinContact(contactRow));
        dropdownMenu.addView(pinIcon);

        ImageView editIcon = new ImageView(this);
        editIcon.setImageResource(android.R.drawable.ic_menu_edit);
        editIcon.setLayoutParams(iconParams);
        editIcon.setOnClickListener(v -> showEditContactDialog(contactRow, contactText, name, number));
        dropdownMenu.addView(editIcon);

        contactRow.addView(dropdownMenu);

        contactRow.setOnClickListener(v -> {
            if (lastOpenedDropdown != null && lastOpenedDropdown != dropdownMenu) {
                lastOpenedDropdown.setVisibility(View.GONE);
                if (lastOpenedContactRow != null) {
                    lastOpenedContactRow.setBackgroundColor(Color.TRANSPARENT);
                }
            }

            if (dropdownMenu.getVisibility() == View.GONE) {
                dropdownMenu.setVisibility(View.VISIBLE);

                ObjectAnimator animatorSlide = ObjectAnimator.ofFloat(dropdownMenu, "translationY", -100f, 0f);
                animatorSlide.setDuration(300);
                animatorSlide.setInterpolator(new AccelerateDecelerateInterpolator());
                animatorSlide.start();

                contactRow.setBackground(getRoundedBackground("#A080A0"));

                lastOpenedDropdown = dropdownMenu;
                lastOpenedContactRow = contactRow;
            } else {
                dropdownMenu.setVisibility(View.GONE);
                contactRow.setBackgroundColor(Color.TRANSPARENT);
                lastOpenedDropdown = null;
                lastOpenedContactRow = null;
            }
        });

        contactListContainer.addView(contactRow);
    }

    private GradientDrawable getRoundedBackground(String color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(30);
        return drawable;
    }

    private void deleteContact(LinearLayout contactRow, String name, String number) {
        database.delete("contacts", "name=? AND number=?", new String[]{name, number});
        contactListContainer.removeView(contactRow);
    }

    private void pinContact(LinearLayout contactRow) {
        Toast.makeText(this, "Pinned contact feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void showEditContactDialog(LinearLayout contactRow, TextView contactText, String name, String number) {
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
        numberInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)});
        layout.addView(numberInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            String newNumber = numberInput.getText().toString().trim();

            if (!newName.isEmpty() && !newNumber.isEmpty() && newNumber.length() == 9) {
                updateContactInDatabase(name, number, newName, newNumber);
                contactText.setText("Name: " + newName + "\nNumber: " + newNumber);
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
