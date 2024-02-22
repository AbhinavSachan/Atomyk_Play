package com.atomykcoder.atomykplay.classes

import android.os.Build
import android.telephony.TelephonyCallback
import android.telephony.TelephonyCallback.CallStateListener
import androidx.annotation.RequiresApi

@RequiresApi(api = Build.VERSION_CODES.S)
open class PhoneStateCallback : TelephonyCallback(), CallStateListener {
    override fun onCallStateChanged(state: Int) {}
}
