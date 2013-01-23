
package org.moresbycoffee.android.maps;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SampleActivity extends android.support.v4.app.FragmentActivity {
    private static final int DEFAULT_FPS = 10;
    private static final int DEFAULT_FREQUENCY = 2000;
    final static LatLng BUDAPEST = new LatLng(47.504265, 19.046098);
    private static final double MOVE_DIFF = 0.0003;

    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;

    private LatLng latlng = BUDAPEST;
    private BlinkingMarker marker;

    private int mFrequency = DEFAULT_FREQUENCY;
    private int mFps = DEFAULT_FPS;
    private boolean mSyncMove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpDebugLayout();
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        addMarker(BUDAPEST, mFps, mFrequency);
    }

    public void moveMarker(View view) {
        latlng = new LatLng(latlng.latitude + MOVE_DIFF, latlng.longitude + MOVE_DIFF);
        marker.moveMarker(latlng, mSyncMove);
    }

    public void setMarker(View view) {
        marker.removeMarker();
        addMarker(latlng, mFps, mFrequency);
    }

    private void addMarker(LatLng position, int fps, int duration) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mapicon_taxi);
        marker = new BlinkingMarker(bitmap, mMap, fps, duration);
        marker.addToMap(BUDAPEST);
        marker.startBlinking();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView
     * MapView}) will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        CameraPosition pos = CameraPosition.builder().target(BUDAPEST).tilt(70).zoom(18f).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
    }

    public void onPause() {
        super.onPause();
        marker.removeMarker();
    }

    private void setUpDebugLayout() {
        CheckBox syncMoveCheckbox = (CheckBox)findViewById(R.id.syncMoveCheckbox);
        syncMoveCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mSyncMove = isChecked;
            }
        });

        SeekBar fpsSeekBar = (SeekBar)findViewById(R.id.fpsSeekBar);
        final TextView mFpsTextView = (TextView)findViewById(R.id.fpsTextView);
        fpsSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFpsTextView.setText(Integer.toString(progress));
                mFps = progress;
            }
        });
        fpsSeekBar.setProgress(mFps);
        mFpsTextView.setText(Integer.toString(mFps));

        SeekBar frequencySeekBar = (SeekBar)findViewById(R.id.frequencySeekBar);
        final TextView frequencyTextView = (TextView)findViewById(R.id.frequencyTextView);
        frequencySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                frequencyTextView.setText(Integer.toString(progress));
                mFrequency = progress;
            }
        });
        frequencySeekBar.setProgress(mFrequency);
        frequencyTextView.setText(Integer.toString(mFrequency));
    }
}
