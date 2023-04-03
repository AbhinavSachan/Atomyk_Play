package com.atomykcoder.atomykplay.classes;

import android.os.Build;
import android.telephony.TelephonyCallback;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.S)
public class PhoneStateCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
    @Override
    public void onCallStateChanged(int state) {

    }
}
