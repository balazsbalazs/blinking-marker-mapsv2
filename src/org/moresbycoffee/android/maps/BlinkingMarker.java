
package org.moresbycoffee.android.maps;

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

/**
 * <p>Workaround for creating a blinking markers with Google Maps V2.</p>
 * 
 * <p>The class represents a Marker, which can simulate a blinking effect.</p>
 * 
 * <p>Internally it creates several bitmaps with different opacity, based on the blinking
 * frequency and the required fps. It adds all the markers to the map with the same position.
 * It simulates the blinking by making different markers visible periodically.</p>
 * 
 * <p>You can move the marker sycnhronously with the blinking. This means that the marker is
 * only moved when the marker is invisible (so the current blink is finished).</p>
 * 
 * <p>Note! You need to be careful with the following things:</p>
 * <ul>
 * <li>You have to call the {@link #stopBlinking()} method on your activity's onPause() method, otherwise
 * blinking thread will continue to work.</li>
 * <li>Use small bitmap for the marker and try to use the minimal possible fps. Increasing the fps will
 * drastically increase the memory consumption of the marker.</li>
 * <li>The default 10 fps and 2 seconds frequency is a reasonable compromise.</li>
 * <li>The marker is attached to a GoogleMap instance and it holds a reference to the map object.</li>
 * </ul>
 */
public class BlinkingMarker extends android.support.v4.app.FragmentActivity {
    private final static String TAG = BlinkingMarker.class.getSimpleName();

    private static int DEFAULT_FPS = 10;
    private static int DEFAULT_FREQUENCY_MILLIS = 2000;

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

    /**
     * <p>Constructor for a blinking marker, with default frequency and fps.</p>
     * @param bitmap - the bitmap for the marker
     * @param map - the GoogleMap instance to which the marker is attached
     */
    public BlinkingMarker(Bitmap bitmap, GoogleMap map) {
        this(bitmap, map, DEFAULT_FPS, DEFAULT_FREQUENCY_MILLIS);
    }

    /**
     * <p>Constructor for a blinking marker, with a custom frequency and fps.</p>
     * @param bitmap - the bitmap for the marker
     * @param map - the GoogleMap instance to which the marker is attached
     * @param fps - the fps of the blinking
     * @param frequencyInMillis - the frequency of the blinking in milliseconds
     */
    public BlinkingMarker(Bitmap bitmap, GoogleMap map, int fps, int frequencyInMillis) {
        mMap = map;
        mOriginalBitmap = bitmap;
        mFps = fps;
        mBlinkPeriodMillis = frequencyInMillis;
        calculateFps(fps, frequencyInMillis);
    }

    /**
     * <p>Add the marker to the Map. Adding a blinking marker means adding
     * several markers with different opacity to the map. At every time only
     * one marker is visible to the user.</p>
     * <p>Note! Have to be called from the UI thread.</p>
     * @param position - the position of the marker
     * @throws IllegalStateException - if it isn't called form the UI thread
     */
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

    /**
     * <p>Removes the marker from the map. It could free up a lot of
     * memory, so use this when you don't need the marker anymore.</p>
     * <p>Note! Have to be called from the UI thread.</p>
     * @throws IllegalStateException - if it isn't called form the UI thread
     */
    public void removeMarker() throws IllegalStateException {
        checkIfUiThread();
        if (mUiHandler != null) {
            stopBlinking();
        }
        removeMarkers();
    }

    /**
     * <p>Moves the marker to a new position, in sync with the blinking.
     * For details see {@link #moveMarker(LatLng, boolean).</p>
     * @param newPosition - the new position
     */
    public void moveMarker(LatLng newPosition) {
        mNewPosition = newPosition;
        mSyncMove = true;
    }

    /**
     * <p>Moves the marker to a new position. The move can be immediate or in 
     * sync with the blinking. If is synchronous then the move will wait until the marker
     * becames invisible and then moves it to the new position.</p>
     * @param newPosition - the new position
     * @param sync - if true the move is in sync with the blinking; if false
     * the move is immediate.
     */
    public void moveMarker(LatLng newPosition, boolean sync) {
        mNewPosition = newPosition;
        mSyncMove = sync;
    }

    /**
     * <p>Starts the blinking of the marker. Don't forget to stop it
     * if your activity goes to the background.</p>
     * @throws IllegalStateException - if it isn't called form the UI thread
     */
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

    /**
     * <p>Stops the blinking of the marker. You sould call this method
     * at least on the onPause() of the Activity.</p>
     * @throws IllegalStateException - if it isn't called form the UI thread
     */
    public void stopBlinking() throws IllegalStateException {
        checkIfUiThread();
        if (mUiHandler == null) {
            return;
        }
        mUiHandler.removeCallbacks(mBlinkerRunnable);
        mUiHandler = null;
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

    private void calculateFps(int fps, int blinkPeriodMillis) {
        mDistinctBitmaps = blinkPeriodMillis * fps / 2 / 1000;
    }

    private Runnable mBlinkerRunnable = new Runnable() {
        @Override
        public void run() {
            if (mCurrentMarkerId == mDistinctBitmaps - 1) {
                mDirection = -1;
            } else if (mCurrentMarkerId == 0) {
                mDirection = 1;
            }
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
