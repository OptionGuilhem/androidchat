package com.example.guilhem.tuto;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;




/**
 * Created by Guilhem on 30/10/2015.
 */
public class SendSMS extends AppCompatActivity{
    EditText msgTxt;
    Button btnSendSMS;
    TextView cname, cnumber;
    String number;
    List<MessageChat> MessageChatList = new ArrayList<>();
    ArrayAdapter<MessageChat> chatArrayAdapter;
    ListView chatMessageListView;
    String phonenumber;
    private BroadcastReceiver smsReceiver;
    String formattime;
    private boolean showTime = false;
    Uri imageUri = Uri.parse("android.resource://com.example.guilhem.tuto/drawable/no_user");
    List<Contact> allContacts;


    @Override
    public void onCreate(Bundle savedInstanceState) {


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int res = prefs.getInt("color", -1);
//        switch (res) {
//            case -1:
//                setTheme(R.style.guilhemdefault);
//                break;
//            case 1:
//                setTheme(R.style.guilhemred);
//                break;
//            case 2:
//                setTheme(R.style.guilhemgreen);
//                break;
//            case 3:
//                setTheme(R.style.guilhemblue);
//                break;
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_sms);
        switch (res) {
            case -1:
                break;
            case 1:
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff550000));
                break;
            case 2:
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff005500));
                break;
            case 3:
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xff000055));
                break;
        }
        Intent intent = getIntent();
        String valuephone = intent.getStringExtra("phone");
        String valuename = intent.getStringExtra("name");
        phonenumber = valuephone;
        msgTxt = (EditText) findViewById(R.id.txtSMSMessage);
        btnSendSMS = (Button) findViewById(R.id.btnSendSMS);
        cname = (TextView) findViewById(R.id.txtSMSName);
        cnumber = (TextView) findViewById(R.id.txtSMSPhone);
        chatMessageListView = (ListView) findViewById(R.id.list_chat);

        cname.setText(valuename);
        cnumber.setText(valuephone);
        number = String.valueOf(cnumber.getText());


        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!number.isEmpty())
                    if (sendSMS())
                    {
                        MessageChat outmsg;
                        String msg = String.valueOf(msgTxt.getText());
                        Date cdate = new Date();
                        String formatdate = DateFormat.getDateTimeInstance().format(cdate);
                        outmsg = new MessageChat(0,msg,formatdate,2);
                        MessageChatList.add(outmsg);
                        msgTxt.setText("");
                        chatArrayAdapter.notifyDataSetChanged();
                    }
                else
                    Toast.makeText(getApplicationContext(), R.string.txtErrorPhone, Toast.LENGTH_SHORT).show();
            }
        });

        Uri smsUri = Uri.parse("content://sms/");
        String[] projection = new String[] {"_id", "address", "body", "date", "type"};
        String address = "address='" + number + "'";
        Cursor cursor = getContentResolver().query(smsUri, projection, address, null, "date ASC");

        if (cursor.moveToFirst()){
            MessageChat current;
            while(!cursor.isAfterLast()){
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                Date date = new Date(cursor.getLong(cursor.getColumnIndex("date")));
                String formatdate = DateFormat.getDateTimeInstance().format(date);
                int type = cursor.getInt(cursor.getColumnIndex("type"));

                current = new MessageChat(id, body, formatdate, type);
                MessageChatList.add(current);
                cursor.moveToNext();
            }
        }
        cursor.close();
        populateList();
        IntentFilter filter = new IntentFilter("incsms");
        smsReceiver = new MyBroadcastReceiver();
        registerReceiver(smsReceiver, filter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

   public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            MessageChat inc;
            String number = intent.getStringExtra("address");
            String message = intent.getStringExtra("message");
            String date = intent.getStringExtra("date");

            if (number.equalsIgnoreCase(phonenumber))
            {
                inc = new MessageChat(0,message, date, 1);
                MessageChatList.add(inc);
                chatArrayAdapter.notifyDataSetChanged();
            }
            else {
                DatabaseHandler dbhandler = new DatabaseHandler(getApplicationContext());
                allContacts = dbhandler.getAllContacts();
                if (!phoneExists(number))
                {
                    Contact contact = new Contact(dbhandler.getContactsCount(),number,number,"","",imageUri);
                    dbhandler.createContact(contact);
                }
            }
        }
    }

    private boolean phoneExists(String number) {
        int contactCount = allContacts.size();
        for(int i = 0; i < contactCount; i++) {
            if(number.equalsIgnoreCase(allContacts.get(i).getPhone()))
                return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("incsms");
        registerReceiver(smsReceiver, filter);
        super.onResume();
    }

    @Override
    public void onPause() {
        unregisterReceiver(smsReceiver);
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        AppTimer.activityStarted();
        if (showTime) {
            Toast.makeText(getApplicationContext(), formattime, Toast.LENGTH_LONG).show();
            showTime = false;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        AppTimer.activityStopped();
        if (AppTimer.show) {
            showTime = true;
            Date ctime = new Date();
            formattime = DateFormat.getTimeInstance().format(ctime);
        }
    }

    private void populateList() {
        chatArrayAdapter = new ChatAdapter();
        chatMessageListView.setAdapter(chatArrayAdapter);
    }

    private class ChatAdapter extends ArrayAdapter<MessageChat> {
        public ChatAdapter() {
            super(SendSMS.this, R.layout.custom_chat, MessageChatList);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.custom_chat, parent, false);

            MessageChat current = MessageChatList.get(position);

            TextView chatText = (TextView) view.findViewById(R.id.txtChatMsg);
            TextView chatDate = (TextView) view.findViewById(R.id.txtChatDate);
            TextView chatStatus = (TextView) view.findViewById(R.id.txtChatStatus);

            chatText.setText(current.getMessage());
            chatDate.setText(current.getDate());
            switch (current.getType()) {
                case 1:
                    chatStatus.setText(R.string.txtReceived);
                    break;
                case 2:
                    chatStatus.setText(R.string.txtSent);
                    break;
                default:
                    chatStatus.setText(R.string.txtUnknown);
                    break;
            }
            return view;
        }
    }


    public void Showing(Cursor cursor, TableLayout tbl) {

        if (!cursor.moveToFirst())
            return;



        TableRow headerRow = new TableRow(this);
        for (int j = 0; j < cursor.getColumnCount(); j++) {
            TextView textView = new TextView(this);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(cursor.getColumnName(j));
            textView.setPadding(0, 0, 5, 0);
            textView.setAlpha(0.8f);
            headerRow.addView(textView);
        }
        headerRow.setPadding(10,10,10,10);
        tbl.addView(headerRow);

        for (int i = 0;i < cursor.getCount(); i++) {
            TableRow tableRow = new TableRow(this);

            for (int j = 0; j < cursor.getColumnCount(); j++) {
                TextView textView = new TextView(this);
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                textView.setText(cursor.getString(j));
                textView.setPadding(0, 0, 5, 0);
                tableRow.addView(textView);
            }
            tableRow.setPadding(10,10,10,10);
            tbl.addView(tableRow);
            cursor.moveToNext();
        }
        cursor.close();
    }


    public boolean sendSMS() {
        try {

            SmsManager sm = SmsManager.getDefault();
            String msg = String.valueOf(msgTxt.getText());
            sm.sendTextMessage(number, null, msg, null, null);
            Toast.makeText(getApplicationContext(), R.string.toastSmsSent,
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    R.string.toastSmsFailed,
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
