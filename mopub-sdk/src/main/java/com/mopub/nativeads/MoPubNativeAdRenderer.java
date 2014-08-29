package com.mopub.nativeads;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mopub.common.VisibleForTesting;
import com.mopub.common.util.MoPubLog;

import java.util.WeakHashMap;

import static android.view.View.VISIBLE;

/**
 * An implementation of {@link com.mopub.nativeads.MoPubAdRenderer} for rendering native ads.
 */
public class MoPubNativeAdRenderer implements MoPubAdRenderer<NativeResponse> {

    private final ViewBinder mViewBinder;

    // This is used instead of View.setTag, which causes a memory leak in 2.3
    // and earlier: https://code.google.com/p/android/issues/detail?id=18273
    private final WeakHashMap<View, NativeViewHolder> mViewHolderMap;

    /**
     * Constructs a native ad renderer with a view binder.
     *
     * @param viewBinder The view binder to use when inflating and rendering an ad.
     */
    public MoPubNativeAdRenderer(final ViewBinder viewBinder) {
        mViewBinder = viewBinder;
        mViewHolderMap = new WeakHashMap<View, NativeViewHolder>();
    }

    @Override
    public View createAdView(final Context context, final ViewGroup parent) {
        return LayoutInflater
                .from(context)
                .inflate(mViewBinder.layoutId, parent, false);
    }

    @Override
    public void renderAdView(final View view, final NativeResponse nativeResponse) {
        final NativeViewHolder nativeViewHolder = getOrCreateNativeViewHolder(view, mViewBinder);

        if (nativeViewHolder == null) {
            MoPubLog.d("Could not create NativeViewHolder.");
            return;
        }

        // Clean up previous state of view
        removeClickListeners(view, nativeViewHolder);

        populateConvertViewSubViews(view, nativeViewHolder, nativeResponse, mViewBinder);
        attachClickListeners(view, nativeViewHolder, nativeResponse);
        view.setVisibility(VISIBLE);
        nativeResponse.prepareImpression(view);
    }

    @VisibleForTesting
    NativeViewHolder getOrCreateNativeViewHolder(final View view, final ViewBinder viewBinder) {
        // Create view holder and put it in the view tag
        NativeViewHolder nativeViewHolder = mViewHolderMap.get(view);
        if (nativeViewHolder == null) {
            nativeViewHolder = NativeViewHolder.fromViewBinder(view, viewBinder);
            mViewHolderMap.put(view, nativeViewHolder);
            return nativeViewHolder;
        }
        return nativeViewHolder;
    }

    private void populateConvertViewSubViews(final View view,
            final NativeViewHolder nativeViewHolder,
            final NativeResponse nativeResponse,
            final ViewBinder viewBinder) {
        nativeViewHolder.update(nativeResponse);
        nativeViewHolder.updateExtras(view, nativeResponse, viewBinder);
    }

    private void removeClickListeners(final View view, final NativeViewHolder nativeViewHolder) {
        view.setOnClickListener(null);
        setCtaClickListener(nativeViewHolder, null);
    }

    private void attachClickListeners(final View view,
            final NativeViewHolder nativeViewHolder,
            final NativeResponse nativeResponse) {
        final NativeViewClickListener nativeViewClickListener =
                new NativeViewClickListener(nativeResponse);
        view.setOnClickListener(nativeViewClickListener);
        setCtaClickListener(nativeViewHolder, nativeViewClickListener);
    }

    private void setCtaClickListener(final NativeViewHolder nativeViewHolder,
            final NativeViewClickListener nativeViewClickListener) {
        // CTA widget could be a button and buttons don't inherit click listeners from parents
        // So we have to set it manually here if so
        if (nativeViewHolder.callToActionView != null
                && nativeViewHolder.callToActionView instanceof Button) {
            nativeViewHolder.callToActionView.setOnClickListener(nativeViewClickListener);
        }
    }

    @VisibleForTesting
    static class NativeViewClickListener implements View.OnClickListener {
        private final NativeResponse mNativeResponse;

        NativeViewClickListener(final NativeResponse nativeResponse) {
            mNativeResponse = nativeResponse;
        }

        @Override
        public void onClick(final View view) {
            mNativeResponse.handleClick(view);
        }
    }
}
