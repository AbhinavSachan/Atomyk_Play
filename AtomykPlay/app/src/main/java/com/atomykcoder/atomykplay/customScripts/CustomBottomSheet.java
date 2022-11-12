package com.atomykcoder.atomykplay.customScripts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class CustomBottomSheet<V extends View> extends BottomSheetBehavior<V> {

    private boolean enableCollapse = false;

    public CustomBottomSheet() {
    }

    public CustomBottomSheet(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void isEnableCollapse(boolean enableCollapse) {
        this.enableCollapse = enableCollapse;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
        if (enableCollapse) {
            return false;
        }
        return super.onInterceptTouchEvent(parent, child, event);
    }
}
