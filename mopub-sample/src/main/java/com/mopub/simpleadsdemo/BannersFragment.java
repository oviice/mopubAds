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
import com.mopub.mobileads.MoPubView;
import com.mopub.mobileads.MoPubView.BannerAdListener;

public class BannersFragment extends Fragment implements BannerAdListener {
    private MoPubView mBannerView;
    private EditText mBannerAdUnitField;

    private MoPubView mMrectView;
    private EditText mMrectAdUnitField;

    private EditText mKeywordsField;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	
        OnClickListener fieldFocusListener = new OnClickListener() {
            public void onClick(View view) {
            	view.requestFocusFromTouch();
            }
        };
    	
        View bannerTabView = inflater.inflate(R.layout.banners, container, false);
        
        mBannerView = (MoPubView) bannerTabView.findViewById(R.id.banner_view);
        mBannerAdUnitField = (EditText) bannerTabView.findViewById(R.id.banner_adunit_field);
        hideSoftKeyboard(mBannerAdUnitField);
        
        mBannerAdUnitField.setOnClickListener(fieldFocusListener);

        mMrectView = (MoPubView) bannerTabView.findViewById(R.id.mrect_view);
        mMrectAdUnitField = (EditText) bannerTabView.findViewById(R.id.mrect_adunit_field);
        
        mMrectAdUnitField.setOnClickListener(fieldFocusListener);

        hideSoftKeyboard(mMrectAdUnitField);

        mKeywordsField = (EditText) bannerTabView.findViewById(R.id.keywords_field);
        hideSoftKeyboard(mKeywordsField);

        Button bannerLoadButton = (Button) bannerTabView.findViewById(R.id.banner_load_button);
        bannerLoadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String adUnitId = mBannerAdUnitField.getText().toString();
                String keywords = mKeywordsField.getText().toString();

                loadMoPubView(mBannerView, adUnitId, keywords);
            }
        });

        Button mrectLoadButton = (Button) bannerTabView.findViewById(R.id.mrect_load_button);
        mrectLoadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String adUnitId = mMrectAdUnitField.getText().toString();
                String keywords = mKeywordsField.getText().toString();

                loadMoPubView(mMrectView, adUnitId, keywords);
            }
        });
        
        return bannerTabView;
    }

    @Override
	public void onDestroy() {
        if (mBannerView != null) {
            mBannerView.destroy();
        }
        if (mMrectView != null) {
            mMrectView.destroy();
        }
        super.onDestroy();
    }

    private void loadMoPubView(MoPubView moPubView, String adUnitId, String keywords) {
        if (moPubView == null) {
            logToast(getActivity(), "Unable to inflate MoPubView from xml.");
            return;
        }

        try {
            validateAdUnitId(adUnitId);
        } catch (IllegalArgumentException exception) {
            logToast(getActivity(), exception.getMessage());
            return;
        }

        moPubView.setBannerAdListener(this);
        moPubView.setAdUnitId(adUnitId);
        moPubView.setKeywords(keywords);
        moPubView.loadAd();
    }

    /*
     * MoPubBanner.BannerAdListener implementation
     */
    @Override
    public void onBannerLoaded(MoPubView moPubView) {
        logToast(getActivity(), "Banner loaded callback.");
    }

    @Override
    public void onBannerFailed(MoPubView moPubView, MoPubErrorCode errorCode) {
        logToast(getActivity(), "Banner failed callback with: " + errorCode.toString());
    }

    @Override
    public void onBannerClicked(MoPubView moPubView) {
        logToast(getActivity(), "Banner clicked callback.");
    }

    @Override
    public void onBannerExpanded(MoPubView moPubView) {
        logToast(getActivity(), "Banner expanded callback.");
    }

    @Override
    public void onBannerCollapsed(MoPubView moPubView) {
        logToast(getActivity(), "Banner collapsed callback.");
    }
}
