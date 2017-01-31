package com.example.guilhem.tuto;

/**
 * Created by Guilhem on 04/11/2015.
 */

public class MessageChat {

    private String _message, _date;
    private int _id, _type;


    public MessageChat(int id, String message, String date, int type) {

        _id = id;
        _message = message;
        _date = date;
        _type = type;
    }

    public String getMessage() {
        return _message;
    }

    public int getId() {
        return _id;
    }

    public String getDate() {
        return _date;
    }

    public int getType() { return _type;}


    public void setType(int type) { this._type = type; }

    public void setMessage(String message) {
        this._message = message;
    }

    public void setDate(String date) {
        this._date = date;
    }
}