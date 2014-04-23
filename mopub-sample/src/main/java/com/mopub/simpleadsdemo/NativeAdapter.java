package com.mopub.simpleadsdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mopub.nativeads.AdapterHelper;
import com.mopub.nativeads.NativeResponse;
import com.mopub.nativeads.ViewBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.mopub.nativeads.MoPubNative.MoPubNativeListener;
import static com.mopub.simpleadsdemo.NativeFragment.MoPubNativeConsumptionListener;

final class NativeAdapter extends BaseAdapter {
    private static final int NUMBER_OF_ROWS = 100;
    private static final int NATIVE_AD_START_POSITION = 10;
    private static final int NATIVE_AD_REPEAT = 7;
    private static final int CONTENT_LAYOUT = R.layout.text_row;

    private final Context mContext;

    // Listener to notify the activity of ad events
    private final MoPubNativeListener mMoPubNativeListener;

    // Specific listener to notify the activity when a native response has been consumed
    // from the mNativeResponses queue
    // This lets the activity request another native ad to replenish its supply
    private final MoPubNativeConsumptionListener mMoPubNativeConsumptionListener;

    // Binds native ad view ids to fields expected from the native response
    private final ViewBinder mViewBinder;

    // Helps with calculating native ad positions in the list and populating views with
    // native response content
    private final AdapterHelper mAdapterHelper;

    // Sample content to be displayed in the list
    private final List<String> mBackingList;

    // Queue of native responses retrieved from the network and ready to be displayed in the app
    private final Queue<NativeResponse> mNativeResponses;

    // Mapping of native responses to position in the list where they have been displayed
    private final HashMap<Integer, Object> mPositionToResponse;

    private enum RowType {
        CONTENT(0),
        NATIVE_AD(1),
        EMPTY_AD(2);

        private static final int size = values().length;
        private final int id;

        private RowType(int id) {
            this.id = id;
        }
    }

    NativeAdapter(final Context context,
            final MoPubNativeListener moPubNativeListener,
            final MoPubNativeConsumptionListener moPubNativeConsumptionListener,
            final ViewBinder viewBinder) {
        super();

        mContext = context.getApplicationContext();
        mMoPubNativeListener = moPubNativeListener;
        mMoPubNativeConsumptionListener = moPubNativeConsumptionListener;
        mViewBinder = viewBinder;

        mAdapterHelper = new AdapterHelper(context, NATIVE_AD_START_POSITION, NATIVE_AD_REPEAT);
        mBackingList = createBackingList(NUMBER_OF_ROWS);

        mNativeResponses = new LinkedList<NativeResponse>();
        mPositionToResponse = new HashMap<Integer, Object>();
    }

    @Override
    public int getCount() {
        return mAdapterHelper.shiftedCount(NUMBER_OF_ROWS);
    }

    @Override
    public String getItem(int position) {
        return mBackingList.get(mAdapterHelper.shiftedPosition(position));
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return RowType.size;
    }

    @Override
    public int getItemViewType(int position) {
        if (mAdapterHelper.isAdPosition(position)) {
            Object nativeResponse = mPositionToResponse.get(position);

            // If we have a native response already assigned to this position in the list then
            // we can use it again
            if (nativeResponse instanceof NativeResponse) {
                return RowType.NATIVE_AD.id;

            // If object is null it implies that this position in the list has yet to be assigned
            // as a native response or empty response
            // If we have a native response ready to be shown in the queue, then assign this
            // position in the list a native response
            } else if (nativeResponse == null && !mNativeResponses.isEmpty()) {
                return RowType.NATIVE_AD.id;

            // If we get here it means that the position in the list was either already an
            // empty response or we didn't have a native response ready to show
            } else {
                return RowType.EMPTY_AD.id;
            }
        } else {
            return RowType.CONTENT.id;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == RowType.NATIVE_AD.id) {
            return getNativeAdView(position, convertView, parent);
        } else if (itemViewType == RowType.EMPTY_AD.id) {
            return getEmptyAdView(position, convertView, parent);
        } else {
            return getContentView(position, convertView, parent);
        }
    }

    private View getNativeAdView(int position, View convertView, ViewGroup parent) {
        // Check to see if we have a native response already used at this position
        Object nativeResponse = mPositionToResponse.get(position);
        if (nativeResponse == null) {

            // If we don't then get a native response from the queue and assign it to this position
            // in the LruCache
            nativeResponse = mNativeResponses.poll();
            mPositionToResponse.put(position, nativeResponse);

            // Notify the activity that we consumed a native response from the queue so it can
            // fetch another one
            mMoPubNativeConsumptionListener
                    .onNativeResponseConsumed((NativeResponse) nativeResponse);
        }

        View view = mAdapterHelper.getAdView(
                convertView,
                parent,
                (NativeResponse) nativeResponse,
                mViewBinder,
                mMoPubNativeListener);

        return view;
    }

    private View getEmptyAdView(int position, View convertView, ViewGroup parent) {
        // Use a linear layout with height of 0 to display nothing for this row
        if (convertView == null) {
            convertView = LayoutInflater
                    .from(mContext)
                    .inflate(R.layout.empty_ad_row, parent, false);
        }
        mPositionToResponse.put(position, new EmptyNativeResponse());
        return convertView;
    }

    private View getContentView(int position, View convertView, ViewGroup parent) {
        // Display app content
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(CONTENT_LAYOUT, parent, false);
        }

        TextView label = (TextView) convertView.findViewById(R.id.my_text_view);
        label.setText(getItem(position));

        return convertView;
    }

    public void addNativeResponse(NativeResponse nativeResponse) {
        // Add more native responses to the queue to be displayed in the app
        mNativeResponses.add(nativeResponse);
    }

    private List<String> createBackingList(int numberOfRows) {
        // Create sample content
        List<String> list = new ArrayList<String>(numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            list.add(String.valueOf(i));
        }
        return list;
    }

    // Empty class to denote an empty ad view in the list
    private static final class EmptyNativeResponse {}
}
