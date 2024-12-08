package com.example.alertify;

import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;

public class SosContacts extends AppCompatActivity {

    private static final int REQUEST_SELECT_CONTACT = 1;
    private LinearLayout contactListContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos_contact_list);

        contactListContainer = findViewById(R.id.contact_list_container);
        Button addContactButton = findViewById(R.id.add_contact_button);
        addContactButton.setOnClickListener(view -> openContactsApp());
    }

    private void openContactsApp() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_SELECT_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_CONTACT && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                fetchContactDetails(contactUri);
            }
        }
    }

    private void fetchContactDetails(Uri contactUri) {
        ContentResolver contentResolver = getContentResolver();

        // Query contact details
        Cursor cursor = contentResolver.query(contactUri,
                new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

            if (idIndex != -1 && nameIndex != -1) {
                String contactId = cursor.getString(idIndex);
                String name = cursor.getString(nameIndex);

                String phoneNumber = null;

                // Query phone numbers
                Cursor phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{contactId},
                        null
                );

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    int phoneIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    if (phoneIndex != -1) {
                        phoneNumber = phoneCursor.getString(phoneIndex);
                    }
                    phoneCursor.close();
                }

                Bitmap photo = getContactPhoto(contactId);

                // Add the contact if valid
                if (name != null && phoneNumber != null) {
                    addContactToView(name, phoneNumber, photo);
                } else {
                    Toast.makeText(this, "Unable to fetch contact details.", Toast.LENGTH_SHORT).show();
                }
            }
            cursor.close();
        } else {
            Toast.makeText(this, "Failed to fetch contact data.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getContactPhoto(String contactId) {
        Uri photoUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contactId);
        InputStream photoInputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                getContentResolver(), photoUri);

        if (photoInputStream != null) {
            return BitmapFactory.decodeStream(photoInputStream);
        }
        return null; // Return null if no photo
    }

    private void addContactToView(String name, String number, Bitmap photo) {
        LinearLayout contactRow = new LinearLayout(this);
        contactRow.setOrientation(LinearLayout.HORIZONTAL);
        contactRow.setPadding(16, 16, 16, 16);
        contactRow.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        // Add the contact photo
        ImageView contactIcon = new ImageView(this);
        if (photo != null) {
            contactIcon.setImageBitmap(photo);
        } else {
            contactIcon.setImageResource(R.drawable.ic_person); // Default icon
        }
        contactIcon.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        contactRow.addView(contactIcon);

        // Add name and number
        TextView contactText = new TextView(this);
        contactText.setText(name + "\n" + number);
        contactText.setTextSize(18);
        contactText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        contactRow.addView(contactText);

        // Dropdown button
        ImageView dropdownButton = new ImageView(this);
        dropdownButton.setImageResource(R.drawable.t_dots); // Replace with your 3-dots icon
        dropdownButton.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
        dropdownButton.setOnClickListener(v -> showDropdownMenu(dropdownButton, contactRow, name, number));
        contactRow.addView(dropdownButton);

        contactListContainer.addView(contactRow);
    }

    private void showDropdownMenu(View anchor, LinearLayout contactRow, String name, String number) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dropdownView = inflater.inflate(R.layout.dropdown_layout, null);

        PopupWindow popupWindow = new PopupWindow(dropdownView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setElevation(10);

        dropdownView.findViewById(R.id.btn_pin).setOnClickListener(v -> {
            Toast.makeText(this, "Pin feature coming soon!", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        dropdownView.findViewById(R.id.btn_edit).setOnClickListener(v -> {
            Toast.makeText(this, "Edit in Contacts app coming soon!", Toast.LENGTH_SHORT).show();
            popupWindow.dismiss();
        });

        dropdownView.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            contactListContainer.removeView(contactRow);
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(anchor, -50, 0);
    }
}
