package com.android.rishab.secure2.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.android.rishab.secure2.FirebaseUI.FirebaseListAdapter;
import com.android.rishab.secure2.R;
import com.android.rishab.secure2.models.contacts;
import com.android.rishab.secure2.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ShowContacts extends AppCompatActivity {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private Uri uriContact;
    private String contactID;// contacts unique ID
    String MycontactNumber , MycontactName, cname, cno;

    private TextView phone, con_name;

    private EditText phone_edit, message_edit;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_contacts);

        ListView contactView = (ListView)findViewById(R.id.contact_list);

        FloatingActionButton addcontacts = (FloatingActionButton)findViewById(R.id.add_contacts);
        addcontacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
                        REQUEST_CODE_PICK_CONTACTS);


            }
        });


        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference GetContactRef;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        GetContactRef = database.getReference(Constants.FIREBASE_LOCATION_USERS).child(uid);

        final DatabaseReference contempReference  = GetContactRef.child(Constants.FIREBASE_LOCATION_CONTACTS);

        FirebaseListAdapter<contacts> mAdapter = new FirebaseListAdapter<contacts>(this,contacts.class,android.R.layout.two_line_list_item,contempReference) {
            @Override
            protected void populateView(View v, contacts model, int position) {
                ((TextView) v.findViewById(android.R.id.text1)).setText(model.getMycontact_name());
                ((TextView) v.findViewById(android.R.id.text2)).setText(model.getMymobile_no());

            }
        };
        contactView.setAdapter(mAdapter);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d(TAG, "Response: " + data.toString());
            uriContact = data.getData();

            cname = retrieveContactName();
            cno = retrieveContactNumber();
            pushContact(cname, cno);
        }
    }


    private String retrieveContactNumber() {

        MycontactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);



        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            MycontactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        MycontactNumber = convert(MycontactNumber);

        String new91 = "+91";
        MycontactNumber = new91 + MycontactNumber;

        Log.e("Check No", "Contact Phone Number: " + MycontactNumber);
        //phone.setText(MycontactNumber);

        return MycontactNumber;
    }


    private String convert(String phoneNo){

        if (phoneNo.contains(" ")) {
            phoneNo = phoneNo.replace(" ", "");
        }
        if (phoneNo.contains("-")) {
            phoneNo = phoneNo.replace("-", "");
        }
        if (phoneNo.length() >= 10) {
            if (phoneNo.contains("-")) {
                phoneNo = phoneNo.substring(phoneNo.length() - 10, phoneNo.length());
                if (phoneNo.startsWith("0")) {
                    phoneNo = phoneNo.substring(1);
                }
            } else {
                phoneNo = phoneNo.substring(phoneNo.length() - 10, phoneNo.length());
                if (phoneNo.startsWith("0")) {
                    phoneNo = phoneNo.substring(1);
                }
            }
        }
        return phoneNo;
    }

    private String add91(String finalNo){

        return finalNo;
    }



    private String retrieveContactName() {

        MycontactName = null;

        // querying contact data store
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);

        if (cursor.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            MycontactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }

        cursor.close();

        Log.d(TAG, "Contact Name: " + MycontactName);
        //con_name.setText(MycontactName);

        return MycontactName;

    }


    private void pushContact(final String ConName, final String mobNo){



        FirebaseDatabase database = FirebaseDatabase.getInstance().getInstance();
        DatabaseReference myRef;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        myRef = database.getReference(Constants.FIREBASE_LOCATION_USERS).child(uid);
        final DatabaseReference ContactRef = myRef.child(Constants.FIREBASE_LOCATION_CONTACTS);


        //DatabaseReference getConId = ContactRef.push();



//        final String cid = getConId.getKey();

        final DatabaseReference mRef = ContactRef.child(cno);




        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() == null){
                    contacts newCont = new contacts(ConName, mobNo);

                    String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());
                    Log.e("Date", date);

                    mRef.setValue(newCont);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
