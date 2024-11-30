package com.example.alertify;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout contactListContainer;
    private SQLiteDatabase database;

    private boolean deleteMode = false;
    private Button cancelDeleteButton;
    private Button confirmDeleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos_contact_list);

        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        contactListContainer = findViewById(R.id.contact_list_container);
        Button addContactButton = findViewById(R.id.add_contact_button);
        Button removeContactButton = findViewById(R.id.remove_contact_button);

        addContactButton.setOnClickListener(view -> showAddContactDialog());
        removeContactButton.setOnClickListener(view -> enableDeleteMode());

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
        nameInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        nameInput.setFilters(new InputFilter[]{
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            char currentChar = source.charAt(i);
                            if (!Character.isLetter(currentChar) && !Character.isSpaceChar(currentChar)) {
                                return ""; // Reject invalid characters
                            }
                        }
                        return null; // Accept valid input
                    }
                }
        });
        layout.addView(nameInput);

        EditText numberInput = new EditText(this);
        numberInput.setHint("Phone Number");
        numberInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        numberInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(9)}); // Limit to 9 digits
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

        ImageView contactIcon = new ImageView(this);
        contactIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        contactIcon.setImageResource(R.drawable.ic_person);
        contactRow.addView(contactIcon);

        TextView contactText = new TextView(this);
        contactText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        contactText.setText(name + " - " + number);
        contactText.setTextSize(18);
        contactRow.addView(contactText);

        contactListContainer.addView(contactRow);
        ScrollView scrollView = findViewById(R.id.contact_list_scrollview);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void enableDeleteMode() {
        if (deleteMode) return;
        deleteMode = true;

        cancelDeleteButton = new Button(this);
        confirmDeleteButton = new Button(this);

        cancelDeleteButton.setText("Cancel");
        confirmDeleteButton.setText("Confirm");

        RelativeLayout mainLayout = findViewById(R.id.main);

        RelativeLayout.LayoutParams cancelParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        cancelParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        cancelParams.setMargins(16, 16, 0, 0);
        mainLayout.addView(cancelDeleteButton, cancelParams);

        RelativeLayout.LayoutParams confirmParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        confirmParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        confirmParams.setMargins(0, 16, 16, 0);
        mainLayout.addView(confirmDeleteButton, confirmParams);

        cancelDeleteButton.setOnClickListener(view -> disableDeleteMode());
        confirmDeleteButton.setOnClickListener(view -> deleteSelectedContacts());

        for (int i = 0; i < contactListContainer.getChildCount(); i++) {
            LinearLayout contactRow = (LinearLayout) contactListContainer.getChildAt(i);
            CheckBox checkBox = new CheckBox(this);
            contactRow.addView(checkBox, 0);
        }
    }

    private void disableDeleteMode() {
        deleteMode = false;

        RelativeLayout mainLayout = findViewById(R.id.main);
        mainLayout.removeView(cancelDeleteButton);
        mainLayout.removeView(confirmDeleteButton);

        for (int i = 0; i < contactListContainer.getChildCount(); i++) {
            LinearLayout contactRow = (LinearLayout) contactListContainer.getChildAt(i);
            contactRow.removeViewAt(0);
        }
    }

    private void deleteSelectedContacts() {
        for (int i = 0; i < contactListContainer.getChildCount(); ) {
            LinearLayout contactRow = (LinearLayout) contactListContainer.getChildAt(i);
            CheckBox checkBox = (CheckBox) contactRow.getChildAt(0);

            if (checkBox.isChecked()) {
                TextView contactText = (TextView) contactRow.getChildAt(2);
                String contactInfo = contactText.getText().toString();
                String[] parts = contactInfo.split(" - ");
                String name = parts[0];
                String number = parts[1];

                database.delete("contacts", "name=? AND number=?", new String[]{name, number});
                contactListContainer.removeView(contactRow);
            } else {
                i++;
            }
        }
        disableDeleteMode();
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
