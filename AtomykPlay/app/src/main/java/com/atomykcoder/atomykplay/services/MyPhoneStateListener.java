package com.atomykcoder.atomykplay.services;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class MyPhoneStateListener extends PhoneStateListener {
    public static boolean phoneRinging = false;

    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_IDLE:
                phoneRinging = false;
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                phoneRinging = true;
                break;
        }
    }
}
