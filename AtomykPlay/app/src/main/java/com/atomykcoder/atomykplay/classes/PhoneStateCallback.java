package com.atomykcoder.atomykplay.classes;

import android.os.Build;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.atomykcoder.atomykplay.activities.MainActivity;

@RequiresApi(api = Build.VERSION_CODES.S)
public class PhoneStateCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
    @Override
    public void onCallStateChanged(int state) {

    }
}
