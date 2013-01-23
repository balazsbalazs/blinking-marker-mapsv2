
package org.moresbycoffee.android.mapv2;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BlinkingMarker {
    private final static String TAG = BlinkingMarker.class.getSimpleName();

    private static int DEFAULT_FPS = 10;
    private static int DEFAULT_BLINK_PERIOD_MILLIS = 2000;

    // Dependencies
    private GoogleMap mMap;

    // State
    private int mBlinkPeriodMillis;
    private int mFps;
    private int mDistinctBitmaps;

    private Bitmap mOriginalBitmap;
    private List<Marker> mMarkers;
    private Handler mUiHandler;

    private int mPrevMarkerId;
    private int mCurrentMarkerId;
    private int mDirection;

    private LatLng mNewPosition;
    private boolean mSyncMove;

    public BlinkingMarker(Bitmap bitmap, GoogleMap map) {
        this(bitmap, map, DEFAULT_FPS, DEFAULT_BLINK_PERIOD_MILLIS);
    }

    public BlinkingMarker(Bitmap bitmap, GoogleMap map, int fps, int blinkPeriodMillis) {
        mMap = map;
        mOriginalBitmap = bitmap;
        mFps = fps;
        mBlinkPeriodMillis = blinkPeriodMillis;
        calculateFps(fps, blinkPeriodMillis);
    }

    private void calculateFps(int fps, int blinkPeriodMillis) {
        mDistinctBitmaps = blinkPeriodMillis * fps / 2 / 1000;
    }

    public void addToMap(LatLng position) throws IllegalStateException {
        checkIfUiThread();
        if (mMarkers != null) {
            Log.w(TAG, "Marker was already added.");
            return;
        }

        mMarkers = new ArrayList<Marker>();
        for (int i = 0; i < mDistinctBitmaps; i++) {
            mMarkers.add(addMarker(adjustOpacity(mOriginalBitmap, 255 / mDistinctBitmaps * i), position));
        }
    }

    public void removeMarker() throws IllegalStateException {
        checkIfUiThread();
        if (mUiHandler != null) {
            stopBlinking();
        }
        removeMarkers();
    }

    private void removeMarkers() {
        if (mMarkers == null) {
            return;
        }

        for (Marker marker : mMarkers) {
            marker.remove();
        }
        mMarkers = null;
    }

    public void moveMarker(LatLng newPosition) {
        mNewPosition = newPosition;
        mSyncMove = true;
    }

    public void moveMarker(LatLng newPosition, boolean sync) {
        mNewPosition = newPosition;
        mSyncMove = sync;
    }

    public void startBlinking() throws IllegalStateException {
        checkIfUiThread();
        if (mUiHandler != null) {
            Log.w(TAG, "Marker was already added.");
            return;
        }

        mUiHandler = new Handler();
        mCurrentMarkerId = mDistinctBitmaps - 1;
        mPrevMarkerId = mDistinctBitmaps - 1;
        mDirection = -1;

        mUiHandler.post(mBlinkerRunnable);
    }

    public void stopBlinking() throws IllegalStateException {
        checkIfUiThread();
        if (mUiHandler == null) {
            return;
        }
        // todo: check ui thread
        mUiHandler.removeCallbacks(mBlinkerRunnable);
        mUiHandler = null;
    }

    private Runnable mBlinkerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentMarkerId == mDistinctBitmaps - 1) {
                mDirection = -1;
            } else if (mCurrentMarkerId == 0) {
                mDirection = 1;
            }
            // TODO: do this with AtomicReference
            final LatLng newPosition = mNewPosition;
            if (newPosition != null && (!mSyncMove || mCurrentMarkerId == 0)) {
                changeMarkerVisibility(mCurrentMarkerId, mPrevMarkerId, newPosition);
                mNewPosition = null;
            } else {
                changeMarkerVisibility(mCurrentMarkerId, mPrevMarkerId, null);
            }
            mPrevMarkerId = mCurrentMarkerId;
            mCurrentMarkerId += mDirection;
            mUiHandler.postDelayed(mBlinkerRunnable, 1000 / mFps);
        }
    };

    private void moveMarkers(final LatLng newPosition) {
        for (Marker marker : mMarkers) {
            marker.setPosition(newPosition);
        }
    }

    private void changeMarkerVisibility(final int visibleMarker, final int invisibleMarker, final LatLng newLocation) {
        mMarkers.get(visibleMarker).setVisible(true);
        mMarkers.get(invisibleMarker).setVisible(false);
        if (newLocation != null)
            moveMarkers(newLocation);
    }

    private Marker addMarker(Bitmap bitmap, LatLng position) {
        MarkerOptions markerOptions = new MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        Marker marker = mMap.addMarker(markerOptions);
        marker.setVisible(false);
        return marker;
    }

    private Bitmap adjustOpacity(Bitmap bitmap, int opacity) {
        Bitmap mutableBitmap = bitmap.isMutable() ? bitmap : bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int colour = (opacity & 0xFF) << 24;
        canvas.drawColor(colour, PorterDuff.Mode.DST_IN);
        return mutableBitmap;
    }

    private void checkIfUiThread() throws IllegalStateException {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("This call has to be made from the UI thread.");
        }
    }
}
