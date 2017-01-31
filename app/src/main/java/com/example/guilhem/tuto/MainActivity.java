package com.example.guilhem.tuto;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;





public class MainActivity extends AppCompatActivity  {

    private static final int EDIT = 2, DELETE = 3, SENDSMS = 1, CALL = 0;

    EditText nameTxt, phoneTxt, emailTxt, addressTxt;
    ImageView contactImageImgView;
    List<Contact> Contacts = new ArrayList<>();
    ListView contactListView;
    Uri imageUri = Uri.parse("android.resource://com.example.guilhem.tuto/drawable/no_user");
    DatabaseHandler dbHandler;
    int longClickItemIndex;
    int shortClickItemIndex;
    ArrayAdapter<Contact> contactAdapter;
    private boolean isEdit = false;
    String formattime;
    private boolean showTime = false;
    private BroadcastReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
        setContentView(R.layout.activity_main);
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

        nameTxt = (EditText) findViewById(R.id.txtName);
        phoneTxt = (EditText) findViewById(R.id.txtPhone);
        emailTxt = (EditText) findViewById(R.id.txtEmail);
        addressTxt = (EditText) findViewById(R.id.txtAddress);
        contactListView = (ListView) findViewById(R.id.listView);
        contactImageImgView = (ImageView) findViewById(R.id.imgViewContactImage);
        dbHandler = new DatabaseHandler(getApplicationContext());

        registerForContextMenu(contactListView);
        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                longClickItemIndex = position;
                return false;
            }
        });

        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                shortClickItemIndex = position;
                Contact contact;
                contact = Contacts.get(shortClickItemIndex);
                String phone = contact.getPhone();
                String name = contact.getName();
                Intent intent = new Intent(MainActivity.this, SendSMS.class);
                intent.putExtra("name", name);
                intent.putExtra("phone", phone);
                MainActivity.this.startActivity(intent);
            }
        });

        final TabHost tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator(getString(R.string.tabCreator));
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator(getString(R.string.tabList));
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(1);

        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEdit) {
                    Contact contact = Contacts.get(longClickItemIndex);
                    contact.setName(String.valueOf(nameTxt.getText()));
                    contact.setPhone(String.valueOf(phoneTxt.getText()));
                    contact.setEmail(String.valueOf(emailTxt.getText()));
                    contact.setAddress(String.valueOf(addressTxt.getText()));
                    contact.setImageURI(imageUri);
                    dbHandler.updateContact(contact);
                    isEdit = false;
                    addBtn.setText(R.string.btnADD);
                    contactAdapter.notifyDataSetChanged();
                    resetInfos();
                    isEdit = false;
                    tabHost.setCurrentTab(1);
                } else {
                    Contact contact = new Contact(dbHandler.getContactsCount(), String.valueOf(nameTxt.getText()), String.valueOf(phoneTxt.getText()), String.valueOf(emailTxt.getText()), String.valueOf(addressTxt.getText()), imageUri);
                    if (!contactExists(contact)) {
                        dbHandler.createContact(contact);
                        Contacts.add(contact);
                        contactAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), String.valueOf(nameTxt.getText()) + getString(R.string.txtValidateADD), Toast.LENGTH_SHORT).show();
                        resetInfos();
                        return;
                    }
                    Toast.makeText(getApplicationContext(), String.valueOf(nameTxt.getText()) + getString(R.string.txtErrorADD), Toast.LENGTH_SHORT).show();
                }
            }
        });


        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addBtn.setEnabled(String.valueOf(nameTxt.getText()).trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        contactImageImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.imageSelection)), 1);
            }
        });

        if (dbHandler.getContactsCount() != 0)
            Contacts.addAll(dbHandler.getAllContacts());
        populateList();


        IntentFilter filter = new IntentFilter("incsms");
        smsReceiver = new MyBroadcastReceiver();
        registerReceiver(smsReceiver, filter);


    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderIcon(R.drawable.edit_icon);
        menu.setHeaderTitle(R.string.longClickMenu);
        menu.add(Menu.NONE, CALL, Menu.NONE, R.string.callContact);
        menu.add(Menu.NONE, SENDSMS, Menu.NONE, R.string.sendSMS);
        menu.add(Menu.NONE, EDIT, Menu.NONE, R.string.editContact);
        menu.add(Menu.NONE, DELETE, Menu.NONE, R.string.deleteContact);
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

    public boolean onContextItemSelected(MenuItem item) {
        Contact contact;
       switch (item.getItemId()) {
           case CALL:
               contact = Contacts.get(longClickItemIndex);
               Intent appel = new Intent(Intent.ACTION_CALL);
               appel.setData(Uri.parse("tel:" + contact.getPhone()));
               startActivity(appel);
               break;
           case SENDSMS:
               contact = Contacts.get(longClickItemIndex);
               String phone = contact.getPhone();
               String name = contact.getName();
               Intent intent = new Intent(MainActivity.this, SendSMS.class);
               intent.putExtra("name", name);
               intent.putExtra("phone", phone);
               MainActivity.this.startActivity(intent);
               break;
           case EDIT:
               contact = Contacts.get(longClickItemIndex);
               setEditMode(contact);
               break;
           case DELETE:
               dbHandler.deleteContact(Contacts.get(longClickItemIndex));
               Contacts.remove(longClickItemIndex);
               contactAdapter.notifyDataSetChanged();
               break;
       }
        return super.onContextItemSelected(item);
    }

    public void setEditMode(Contact contact) {
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setCurrentTab(0);
        nameTxt.setText(contact.getName());
        phoneTxt.setText(contact.getPhone());
        emailTxt.setText(contact.getEmail());
        addressTxt.setText(contact.getAddress());
        imageUri = contact.getImageURI();
        contactImageImgView.setImageURI(imageUri);
        Button edit = (Button) findViewById(R.id.btnAdd);
        edit.setText(R.string.btnUpdate);
        isEdit = true;
    }


    private void resetInfos() {
        nameTxt.setText("");
        phoneTxt.setText("");
        emailTxt.setText("");
        addressTxt.setText("");
        contactImageImgView.setImageURI(Uri.parse("android.resource://com.example.guilhem.tuto/drawable/no_user"));
    }

    private boolean contactExists(Contact contact) {
        String name = contact.getName();
        int contactCount = Contacts.size();

        for(int i = 0; i < contactCount; i++) {
            if(name.compareToIgnoreCase(Contacts.get(i).getName()) == 0)
                return true;
        }
        return false;
    }

    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (reqCode == 1) {
                imageUri = data.getData();
                contactImageImgView.setImageURI(imageUri);
            }
        }
    }


    private void populateList() {

        contactAdapter = new ContactListAdapter();
        contactListView.setAdapter(contactAdapter);
    }


    private class ContactListAdapter extends ArrayAdapter<Contact> {
        public ContactListAdapter() {
            super(MainActivity.this, R.layout.listview_item, Contacts);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_item, parent, false);

            Contact currentContact = Contacts.get(position);

            TextView name = (TextView) view.findViewById(R.id.contactName);
            name.setText(currentContact.getName());
            TextView phone = (TextView) view.findViewById(R.id.phoneNumber);
            phone.setText(currentContact.getPhone());
            TextView email = (TextView) view.findViewById(R.id.emailAddress);
            email.setText(currentContact.getEmail());
            TextView address = (TextView) view.findViewById(R.id.cAddress);
            address.setText(currentContact.getAddress());
            ImageView ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);
            ivContactImage.setImageURI(currentContact.getImageURI());

            return view;
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String number = intent.getStringExtra("address");

            List<Contact>allContacts = dbHandler.getAllContacts();
            if (!phoneExists(allContacts, number))
            {
                Contact contact = new Contact(dbHandler.getContactsCount(),number,number,"","",imageUri);
                dbHandler.createContact(contact);
                Contacts.add(contact);
                contactAdapter.notifyDataSetChanged();
            }
        }
    }

    private boolean phoneExists(List<Contact> allContacts, String number) {
        int contactCount = allContacts.size();
        for(int i = 0; i < contactCount; i++) {
            if(number.equalsIgnoreCase(allContacts.get(i).getPhone()))
                return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SubMenu sm = menu.addSubMenu(R.string.color);

        sm.add(Menu.NONE, 1, Menu.NONE, R.string.red);
        sm.add(Menu.NONE, 2, Menu.NONE, R.string.green);
        sm.add(Menu.NONE, 3, Menu.NONE, R.string.blue);
        sm.add(Menu.NONE, -1, Menu.NONE, R.string.white);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Intent intent = new Intent(this,MainActivity.class);
        switch (item.getItemId()) {
            case -1:
                editor.putInt("color", -1);
                editor.apply();
                startActivity(intent);
                finish();
                return true;
            case 1:
                editor.putInt("color", 1);
                editor.apply();
                startActivity(intent);
                finish();
                return true;
            case 2:
                editor.putInt("color", 2);
                editor.apply();
                startActivity(intent);
                finish();
                return true;
            case 3:
                editor.putInt("color", 3);
                editor.apply();
                startActivity(intent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
