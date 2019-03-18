// Copyright 2018-2019 Twitter, Inc.
// Licensed under the MoPub SDK License Agreement
// http://www.mopub.com/legal/sdk-license-agreement/

package com.mopub.mobileads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.mopub.common.AdReport;
import com.mopub.common.VisibleForTesting;

public class ViewGestureDetector extends GestureDetector {
    private final View mView;
    @NonNull
    private AdAlertGestureListener mAdAlertGestureListener;

    public ViewGestureDetector(@NonNull Context context, @NonNull View view, @Nullable AdReport adReport)  {
        this(context, view, new AdAlertGestureListener(view, adReport));
    }

    private ViewGestureDetector(Context context, View view, @NonNull AdAlertGestureListener adAlertGestureListener) {
        super(context, adAlertGestureListener);

        mAdAlertGestureListener = adAlertGestureListener;
        mView = view;

        setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        final boolean result = super.onTouchEvent(motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                mAdAlertGestureListener.finishGestureDetection();
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isMotionEventInView(motionEvent, mView)) {
                    resetAdFlaggingGesture();
                }
                break;

            default:
                break;
        }
        return result;
    }

    void resetAdFlaggingGesture() {
        mAdAlertGestureListener.reset();
    }

    private boolean isMotionEventInView(MotionEvent motionEvent, View view) {
        if (motionEvent == null || view == null) {
            return false;
        }

        float x = motionEvent.getX();
        float y = motionEvent.getY();

        return (x >= 0 && x <= view.getWidth())
                && (y >= 0 && y <= view.getHeight());
    }

    public void onResetUserClick() {
        mAdAlertGestureListener.onResetUserClick();
    }

    public boolean isClicked() {
        return mAdAlertGestureListener.isClicked();
    }

    @Deprecated // for testing
    void setAdAlertGestureListener(@NonNull AdAlertGestureListener adAlertGestureListener) {
        mAdAlertGestureListener = adAlertGestureListener;
    }

    @VisibleForTesting
    public void setClicked(boolean clicked) {
        mAdAlertGestureListener.mIsClicked = clicked;
    }
}
