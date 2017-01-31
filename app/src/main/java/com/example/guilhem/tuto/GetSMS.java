package com.example.guilhem.tuto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Guilhem on 03/11/2015.
 */

public class GetSMS extends BroadcastReceiver {

    public GetSMS () {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs;
        String address = "";
        String message = "";


        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            msgs = new SmsMessage[pdus.length];

            for (int i = 0; i < msgs.length; i++) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String format = bundle.getString("format");
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }
                else {
                    msgs[i] =  SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                address = msgs[i].getOriginatingAddress();
                message += msgs[i].getMessageBody();
            }
            Intent intentmsg = new Intent();
            intentmsg.setAction("incsms");
            intentmsg.putExtra("address", address);
            intentmsg.putExtra("message", message);

            Date cdate = new Date();
            String formatdate = DateFormat.getDateTimeInstance().format(cdate);
            intentmsg.putExtra("date", formatdate);




            context.sendBroadcast(intentmsg);
        }
    }
}
