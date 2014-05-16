/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.simpleadsdemo;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.mopub.common.util.MoPubLog;
import com.mopub.nativeads.MoPubNative;
import com.mopub.nativeads.NativeErrorCode;
import com.mopub.nativeads.NativeResponse;
import com.mopub.nativeads.RequestParameters;
import com.mopub.nativeads.ViewBinder;

import static com.mopub.nativeads.MoPubNative.MoPubNativeListener;
import static com.mopub.simpleadsdemo.Utils.hideSoftKeyboard;
import static com.mopub.simpleadsdemo.Utils.logToast;

public final class NativeFragment extends Fragment {
    private static final int NATIVE_AD_INITIALIZATION_COUNT = 3;

    private final MyMoPubNativeListener mMyMoPubNativeListener;
    private final MyMoPubNativeConsumptionListener mMyMoPubNativeConsumptionListener;

    private ListView mListView;
    private MoPubNative mMoPubNative;
    private EditText mNativeAdUnitField;

    private ViewBinder mViewBinder;
    private NativeAdapter mNativeAdapter;

    public NativeFragment() {
        super();
        mMyMoPubNativeListener = new MyMoPubNativeListener();
        mMyMoPubNativeConsumptionListener = new MyMoPubNativeConsumptionListener();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                 Bundle savedInstanceState) {

        View nativeFragmentView = inflater.inflate(R.layout.nativetab, container, false);

        mListView = (ListView) nativeFragmentView.findViewById(R.id.list_view);

        View.OnClickListener fieldFocusListener = new View.OnClickListener() {
            public void onClick(View view) {
                view.requestFocusFromTouch();
            }
        };

        mNativeAdUnitField = (EditText) nativeFragmentView.findViewById(R.id.native_adunit_field);
        hideSoftKeyboard(mNativeAdUnitField);

        mNativeAdUnitField.setOnClickListener(fieldFocusListener);

        mViewBinder = new ViewBinder.Builder(R.layout.native_ad_row)
                .titleId(R.id.native_title)
                .textId(R.id.native_text)
                .callToActionId(R.id.native_cta)
                .mainImageId(R.id.native_main_image)
                .iconImageId(R.id.native_icon_image)
                .build();

        Button nativeLoadButton = (Button) nativeFragmentView.findViewById(R.id.native_load_button);
        nativeLoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String adUnitId = mNativeAdUnitField.getText().toString();

                // Destory old native ad if it exists
                if(mMoPubNative != null) {
                    mMoPubNative.destroy();
                }

                mMoPubNative = new MoPubNative(getActivity(), adUnitId, mMyMoPubNativeListener);

                // Initialize activity with a number of native ads ready to show
                for (int i=0; i < NATIVE_AD_INITIALIZATION_COUNT; ++i) {
                    requestNativeAd();
                }

                mNativeAdapter = new NativeAdapter(
                        getActivity(),
                        mMyMoPubNativeListener,
                        mMyMoPubNativeConsumptionListener,
                        mViewBinder
                );

                mListView.setAdapter(mNativeAdapter);

                logToast(getActivity(), "Loaded new native ad unit");
            }
        });

        return nativeFragmentView;
    }

    private void requestNativeAd() {
        Location exampleLocation = new Location("example");
        exampleLocation.setLatitude(23.1);
        exampleLocation.setLongitude(42.1);
        exampleLocation.setAccuracy(100);

        RequestParameters requestParameters = new RequestParameters.Builder()
                .keywords("key:value")
                .location(exampleLocation)
                .build();

        mMoPubNative.makeRequest(requestParameters);
    }

    private final class MyMoPubNativeListener implements MoPubNativeListener {
        private static final int EMPTY_RESPONSE_DELAY_MS = 30000; // 30 seconds
        private Handler mHandler;
        private Runnable mRunnable;

        public MyMoPubNativeListener() {
            mHandler = new Handler();
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    requestNativeAd();
                }
            };
        }

        @Override
        public void onNativeLoad(final NativeResponse nativeResponse) {
            MoPubLog.d("MoPubNativeListener: Load!, NativeResponse: " + nativeResponse.toString());

            // When we have a valid response, let the adapter manage when it will be shown
            mNativeAdapter.addNativeResponse(nativeResponse);
        }

        @Override
        public void onNativeFail(final NativeErrorCode errorCode) {
            MoPubLog.d("MoPubNativeListener: Fail!, NativeErrorCode: " + errorCode.toString());
            switch (errorCode) {
                case INVALID_REQUEST_URL:
                    // Invalid url, don't try to request again
                    break;
                case CONNECTION_ERROR:
                    // Data connection was lost, activity should start requesting ads again
                    // when the data connection is regained
                    break;
                default:
                    // For any other error, wait 30 seconds before trying again
                    mHandler.postDelayed(mRunnable, EMPTY_RESPONSE_DELAY_MS);
                    break;
            }
        }

        @Override
        public void onNativeImpression(final View view) {
            MoPubLog.d("MoPubNativeListener: Impression!, View: " + view.toString());
        }

        @Override
        public void onNativeClick(final View view) {
            MoPubLog.d("MoPubNativeListener: Click!, View: " + view.toString());
        }
    }

    public static interface MoPubNativeConsumptionListener {
        public void onNativeResponseConsumed(NativeResponse nativeResponse);
    }

    private final class MyMoPubNativeConsumptionListener implements MoPubNativeConsumptionListener {
        @Override
        public void onNativeResponseConsumed(NativeResponse nativeResponse) {
            // Native ad was displayed in the app, request a new one to always
            // have ads ready to display
            requestNativeAd();
        }
    }
}
