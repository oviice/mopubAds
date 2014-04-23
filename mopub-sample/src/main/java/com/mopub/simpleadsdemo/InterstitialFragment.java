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

import static com.mopub.simpleadsdemo.Utils.hideSoftKeyboard;
import static com.mopub.simpleadsdemo.Utils.logToast;
import static com.mopub.simpleadsdemo.Utils.validateAdUnitId;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubInterstitial.InterstitialAdListener;

public class InterstitialFragment extends Fragment implements InterstitialAdListener {
    private MoPubInterstitial mMoPubInterstitial;
    private EditText mInterstitialAdUnitField;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        View interstitialTabView = inflater.inflate(R.layout.interstitials, container, false);

        OnClickListener fieldFocusListener = new OnClickListener() {
            public void onClick(View view) {
            	view.requestFocusFromTouch();
            }
        };
       
        mInterstitialAdUnitField = (EditText) interstitialTabView.findViewById(R.id.interstitials_edit_text_interstitial);
        mInterstitialAdUnitField.setOnClickListener(fieldFocusListener);
        hideSoftKeyboard(mInterstitialAdUnitField);

        Button interstitialLoadButton = (Button) interstitialTabView.findViewById(R.id.interstitials_load_interstitial);
        interstitialLoadButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String adUnitId = mInterstitialAdUnitField.getText().toString();

                try {
                    validateAdUnitId(adUnitId);

                    mMoPubInterstitial = new MoPubInterstitial(InterstitialFragment.this.getActivity(), adUnitId);
                    mMoPubInterstitial.setInterstitialAdListener(InterstitialFragment.this);
                    mMoPubInterstitial.load();
                } catch (IllegalArgumentException exception) {
                    String message = exception.getMessage();

                    if (message != null) {
                        logToast(InterstitialFragment.this.getActivity(), message);
                    }
                }
            }
        });
        
        Button interstitialShowButton = (Button) interstitialTabView.findViewById(R.id.interstitials_show_interstitial);
        interstitialShowButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mMoPubInterstitial != null && mMoPubInterstitial.isReady()) {
                    mMoPubInterstitial.show();
                } else {
                    logToast(InterstitialFragment.this.getActivity(), "Interstitial was not ready. Try reloading.");
                }
            }
        });
        
        return interstitialTabView;
    }

    @Override
    public void onDestroy() {
        if (mMoPubInterstitial != null) {
            mMoPubInterstitial.destroy();
        }
        super.onDestroy();
    }

    /*
     * MoPubInterstitial.InterstitialAdListener implementation
     */
    @Override
    public void onInterstitialLoaded(MoPubInterstitial interstitial) {
        logToast(getActivity(), "Interstitial loaded successfully.");
    }

    @Override
    public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
        logToast(getActivity(), "Interstitial failed to load with error: " + errorCode.toString());
    }

    @Override
    public void onInterstitialShown(MoPubInterstitial interstitial) {
        logToast(getActivity(), "Interstitial shown.");
    }

    @Override
    public void onInterstitialClicked(MoPubInterstitial interstitial) {
        logToast(getActivity(), "Interstitial clicked.");
    }

    @Override
    public void onInterstitialDismissed(MoPubInterstitial interstitial) {
        logToast(getActivity(), "Interstitial dismissed.");
    }
}
