package com.mopub.common.event;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.mopub.common.VisibleForTesting;
import com.mopub.common.logging.MoPubLog;

public class EventDispatcher {
    private final Iterable<EventRecorder> mEventRecorders;
    private final HandlerThread mHandlerThread;
    private final Handler mMessageHandler;
    private final Handler.Callback mHandlerCallback;

    @VisibleForTesting
    EventDispatcher(Iterable<EventRecorder> recorders, HandlerThread handlerThread) {
        mEventRecorders = recorders;

        mHandlerThread = handlerThread;
        mHandlerThread.start();

        mHandlerCallback = new Handler.Callback() {
            @Override
            public boolean handleMessage(final Message msg) {
                if (msg.obj instanceof BaseEvent) {
                    for (final EventRecorder recorder : mEventRecorders) {
                        recorder.record((BaseEvent) msg.obj);
                    }
                } else {
                    MoPubLog.d("EventDispatcher received non-BaseEvent message type.");
                }
                return true;
            }
        };
        mMessageHandler = new Handler(mHandlerThread.getLooper(), mHandlerCallback);
    }

    void dispatch(BaseEvent event) {
        Message.obtain(mMessageHandler, 0, event).sendToTarget();
    }

    @VisibleForTesting
    Iterable<EventRecorder> getEventRecorders() {
        return mEventRecorders;
    }

    @VisibleForTesting
    Handler.Callback getHandlerCallback() {
        return mHandlerCallback;
    }
}
