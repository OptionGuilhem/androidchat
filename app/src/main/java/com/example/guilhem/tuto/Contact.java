package com.example.guilhem.tuto;

import android.net.Uri;
/**
 * Created by Guilhem on 24/10/2015.
 */
public class Contact {

    private String _name, _phone, _email, _address;
    private Uri _imageURI;
    private int _id;




    public Contact (int id, String name, String phone, String email, String address, Uri imageURI ) {

        _id = id;
        _name = name;
        _phone = phone;
        _email = email;
        _address = address;
        _imageURI = imageURI;
    }

    public String getName() {
        return _name;
    }

    public int getId() { return _id; }

    public String getPhone() {
        return _phone;
    }

    public String getEmail() {
        return _email;
    }

    public String getAddress() {
        return _address;
    }

    public Uri getImageURI() { return _imageURI; }


    public void setName(String name) {
        this._name = name;
    }

    public void setPhone(String phone) {
        this._phone = phone;
    }

    public void setEmail(String email) {
        this._email = email;
    }

    public void setAddress(String address) {
        this._address = address;
    }

    public void setImageURI(Uri imageURI) {
        this._imageURI = imageURI;
    }
}
