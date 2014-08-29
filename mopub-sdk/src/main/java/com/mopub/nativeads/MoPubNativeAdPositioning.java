package com.mopub.nativeads;

import android.util.SparseArray;

import com.mopub.common.util.MoPubLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A positioning object used to specify ad positions in client SDK integration code.
 *
 * Client positioning allows you to:
 * <ul>
 * <li>Specify fixed positions for ads</li>
 * <li>Specify an spacing interval for ads, starting at the last fixed position</li>
 * <li>Override the ad unit for a given fixed ad position. This is useful to
 * direct-sell a "premium" ad unit at a specified position. It can also be useful
 * if you want to track a position separately from other positions in the MoPub UI.</li>
 * </ul>
 *
 * For example, to space ads every 5 items, starting at position 3:
 * <code>
 * MoPubNativeAdPositioning positioning = new MoPubNativeAdPositioning.Builder()
 *     .addFixedPosition(3)
 *     .enableRepeatingPositions(5)
 *     .build();
 * }
 * </code>
 *
 * {@code MoPubNativeAdPositioning} is an immutable class. To create a new instance, use
 * {@link MoPubNativeAdPositioning.Builder}.
 */
public final class MoPubNativeAdPositioning {
    /**
     * Constant for indicating that ad positions should not repeat.
     */
    public static final int NO_REPEAT = -1;

    private final int mRepeatInterval;
    private final List<Integer> mFixedPositions;
    private final SparseArray<String> mAdUnitOverrides;

    private MoPubNativeAdPositioning(final int repeatInterval, final List<Integer> fixedPositions,
            final SparseArray<String> adUnitOverrides) {
        mRepeatInterval = repeatInterval;

        // Safe copy the positions to avoid modification
        mFixedPositions = new ArrayList<Integer>(fixedPositions);

        // Safe copy overrides. 'clone' is protected on pre-ICS devices, so we copy manually.
        mAdUnitOverrides = new SparseArray<String>(adUnitOverrides.size());
        for (int i = 0; i < adUnitOverrides.size(); ++i) {
            mAdUnitOverrides.put(adUnitOverrides.keyAt(i), adUnitOverrides.valueAt(i));
        }
    }

    /**
     * Returns an ordered array of fixed ad positions.
     *
     * @return Fixed ad positions.
     */
    public List<Integer> getFixedPositions() {
        return mFixedPositions;
    }

    /**
     * Returns the repeating ad interval.
     *
     * Repeating ads start after the last fixed position. Returns -1 if there is no repeating
     * interval.
     *
     * @return The repeating ad interval.
     */
    public int getRepeatingInterval() {
        return mRepeatInterval;
    }

    /**
     * Returns the overridden ad unit ID for the given position.
     *
     * Returns {@code null} if the position is not an ad, or if there is no overridden ad unit for
     * this position.
     *
     * @param position The ad position.
     * @return The overridden ad unit ID.
     */
    public String getAdUnitIdOverride(int position) {
        return mAdUnitOverrides.get(position);
    }

    /**
     * Creates and returns a {@code MoPubNativeAdPositioning.Builder}.
     *
     * @return A new builder.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A Builder class for the ad positioning.
     */
    public static final class Builder {
        private int mRepeatInterval = MoPubNativeAdPositioning.NO_REPEAT;
        private final List<Integer> mFixedPositions;
        private final SparseArray<String> mAdUnitIdOverrides;

        private Builder() {
            mFixedPositions = new ArrayList<Integer>();
            mAdUnitIdOverrides = new SparseArray<String>();
        }

        /**
         * Specifies a fixed ad position.
         *
         * @param position The ad position.
         * @return The builder.
         */
        public Builder addFixedPosition(final int position) {
            internalAddFixedPosition(position);
            return this;
        }

        /**
         * Specifies a fixed ad position, with an ad unit ID override.
         *
         * Calling this method twice with the same position and different ad unit IDs replaces the
         * first ad unit ID.
         *
         * @param position The ad position.
         * @param adUnitIdOverride The ad unit ID to use when requesting and ad for this position.
         * @return The builder.
         */
        public Builder addFixedPosition(final int position, final String adUnitIdOverride) {
            if (internalAddFixedPosition(position)) {
                mAdUnitIdOverrides.put(position, adUnitIdOverride);
            }
            return this;
        }

        private boolean internalAddFixedPosition(final int position) {
            if (position < 0) {
                return false;
            }
            if (!mFixedPositions.contains(position)) {
                mFixedPositions.add(position);
            } else {
                mAdUnitIdOverrides.remove(position);
            }
            return true;
        }

        /**
         * Enables showing ads ad at a repeated interval.
         *
         * @param interval The frequency at which to show ads. Must be an integer greater than 1 or
         * the constant NO_REPEAT.
         * @return The builder.
         */
        public Builder enableRepeatingPositions(final int interval) {
            if (interval < 1 && interval != NO_REPEAT) {
                MoPubLog.w("Attempted to assign an illegal interval < 1 to the" +
                        " ad positioning object. Call ignored.");
                return this;
            }
            mRepeatInterval = interval;
            return this;
        }

        /**
         * Creates and returns a new immutable positioning object.
         *
         * @return A new positioning object.
         */
        public MoPubNativeAdPositioning build() {
            // Could insert into a sorted LinkedList instead of doing this.
            Collections.sort(mFixedPositions);
            return new MoPubNativeAdPositioning(mRepeatInterval, mFixedPositions,
                    mAdUnitIdOverrides);
        }
    }
}
