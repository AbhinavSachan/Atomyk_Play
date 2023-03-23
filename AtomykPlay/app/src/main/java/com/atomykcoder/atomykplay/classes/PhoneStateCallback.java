package com.atomykcoder.atomykplay.classes;

import android.os.Build;
import android.telephony.TelephonyCallback;

import androidx.annotation.RequiresApi;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

@RequiresApi(api = Build.VERSION_CODES.S)
public class PhoneStateCallback extends TelephonyCallback implements TelephonyCallback.CallStateListener {
    private MutableLiveData<Integer> state = new MutableLiveData<>();

    public void setState(int s) {
        state.setValue(s);
    }

    public LiveData<Integer> getState() {
        return state;
    }

    @Override
    public void onCallStateChanged(int state) {
        setState(state);
    }
}
