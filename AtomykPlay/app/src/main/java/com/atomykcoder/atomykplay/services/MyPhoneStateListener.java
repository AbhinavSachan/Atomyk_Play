package com.atomykcoder.atomykplay.services;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

public class MyPhoneStateListener extends PhoneStateListener {
    public static boolean phone_ringing = false;

    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_OFFHOOK:
            case TelephonyManager.CALL_STATE_IDLE:
                phone_ringing = false;
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                phone_ringing = true;
                break;
        }
    }
}
