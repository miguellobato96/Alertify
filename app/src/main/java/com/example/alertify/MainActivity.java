package com.example.alertify;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private LinearLayout contactListContainer;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.sos_contact_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        @SuppressWarnings("resource")
        DBHelper dbHelper = new DBHelper(this);
        database = dbHelper.getWritableDatabase();

        contactListContainer = findViewById(R.id.contact_list_container);
        Button addContactButton = findViewById(R.id.add_contact_button);
        Button removeContactButton = findViewById(R.id.remove_contact_button);

        addContactButton.setOnClickListener(view -> showAddContactDialog());
        removeContactButton.setOnClickListener(view -> removeContact());

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
        layout.addView(nameInput);

        EditText numberInput = new EditText(this);
        numberInput.setHint("Phone Number");
        numberInput.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        layout.addView(numberInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String number = numberInput.getText().toString().trim();

            if (!name.isEmpty() && !number.isEmpty()) {
                addContactToDatabase(name, number);
                addContactToView(name, number);
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
        contactText.setText(getString(R.string.contact_display, name, number));
        contactText.setTextSize(18);
        contactRow.addView(contactText);

        contactListContainer.addView(contactRow);
        ScrollView scrollView = findViewById(R.id.contact_list_scrollview);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void removeContact() {
        int childCount = contactListContainer.getChildCount();
        if (childCount > 0) {
            contactListContainer.removeViewAt(childCount - 1);
            database.delete("contacts", "id = (SELECT MAX(id) FROM contacts)", null);
        }
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
