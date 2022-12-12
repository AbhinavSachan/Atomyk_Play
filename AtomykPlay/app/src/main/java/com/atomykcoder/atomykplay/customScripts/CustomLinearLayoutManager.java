package com.atomykcoder.atomykplay.customScripts;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CustomLinearLayoutManager extends LinearLayoutManager {
    private final float mShrinkAmount = 0.15f;
    private final float mShrinkDistance = 0.9f;

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        super.onLayoutChildren(recycler, state);
        scrollHorizontallyBy(0,recycler,state);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int orientation = getOrientation();
        if (orientation == RecyclerView.VERTICAL) {
            int scrolled = super.scrollHorizontallyBy(dy, recycler, state);
            float midPoint = getHeight() / 2.0f;
            float d0 = 0f;
            float d1 = mShrinkDistance * midPoint;
            float s0 = 1f;
            float s1 = 1f - mShrinkAmount;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != null) {
                    float childMid = (getDecoratedBottom(child) + getDecoratedTop(child)) / 2f;
                    float d = Math.min(d1, Math.abs(midPoint - childMid));
                    float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                }
            }
            return scrolled;
        }else {
            return 0;
        }
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int orientation = getOrientation();
        if (orientation == RecyclerView.HORIZONTAL) {
            int scrolled = super.scrollHorizontallyBy(dx, recycler, state);
            float midPoint = getWidth() / 2.0f;
            float d0 = 0f;
            float d1 = mShrinkDistance * midPoint;
            float s0 = 1f;
            float s1 = 1f - mShrinkAmount;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != null) {
                    float childMid = (getDecoratedBottom(child) + getDecoratedTop(child)) / 2f;
                    float d = Math.min(d1, Math.abs(midPoint - childMid));
                    float scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0);
                    child.setScaleX(scale);
                    child.setScaleY(scale);
                }
            }
            return scrolled;
        }else {
            return 0;
        }
    }
}
