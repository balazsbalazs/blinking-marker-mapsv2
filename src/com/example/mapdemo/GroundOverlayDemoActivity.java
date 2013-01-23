/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.TimingLogger;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * This shows how to add a ground overlay to a map.
 */
@SuppressLint("NewApi")
public class GroundOverlayDemoActivity extends android.support.v4.app.FragmentActivity
        implements OnSeekBarChangeListener {

    private static final int TRANSPARENCY_MAX = 255;
    private static final LatLng NEWARK = new LatLng(40.714086, -74.228697);
    private static final LatLng NEWARK2 = new LatLng(40.714286, -74.228497);

    private GoogleMap mMap;
    private GroundOverlay mGroundOverlay;
    private SeekBar mTransparencyBar;
    private int mCurrentTransparency = TRANSPARENCY_MAX;
    private int mTransparencyDirection = 1;
    private Marker mMarker;
    private Marker mMarkerOld;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ground_overlay_demo);

        mTransparencyBar = (SeekBar) findViewById(R.id.transparencySeekBar);
        mTransparencyBar.setMax(TRANSPARENCY_MAX);
        mTransparencyBar.setProgress(0);

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        startAnimation();
    }
    boolean yellow = true;
    private void startAnimation() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (mCurrentTransparency >= TRANSPARENCY_MAX) {
                        mTransparencyDirection = -1;
                    } else if (mCurrentTransparency <= 0) {
                        mTransparencyDirection = 1;
                    }
                    mCurrentTransparency += mTransparencyDirection;
//                    if (mGroundOverlay != null) {
//                        GroundOverlayDemoActivity.this.runOnUiThread(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                mGroundOverlay.setTransparency(mCurrentTransparency/TRANSPARENCY_MAX);
//                            }
//                        });
//                    }
                    mBitmap.eraseColor(Color.argb(mCurrentTransparency, 0, 0, 255));
                    GroundOverlayDemoActivity.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            TimingLogger timings = new TimingLogger("BB", "adding and removing markers");
                            mMarkerOld = mMarker;
                            mMarker = mMap.addMarker(new MarkerOptions()
                            .position(NEWARK2)
                            .title("Brisbane")
                            .snippet("Population: 2,074,200")
                            .icon(BitmapDescriptorFactory.fromBitmap(mBitmap)));
                            timings.addSplit("marker added");
                            mMarkerOld.remove();
                            timings.addSplit("marker removed");
                            timings.dumpToLog();
                        }
                        
                    });
                    SystemClock.sleep(10);
                }
            }
            
        }).start();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            SupportMapFragment fragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
            mMap = fragment.getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(NEWARK, 11));

        mGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.mapicon_taxi)).anchor(0, 1)
                .position(NEWARK, 8600f));
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mapicon_taxi, opt);
        mMarker = mMap.addMarker(new MarkerOptions()
                .position(NEWARK2)
                .title("Brisbane")
                .snippet("Population: 2,074,200")
                .icon(BitmapDescriptorFactory.fromBitmap(mBitmap)));
        mMarker.remove();
        mBitmap.eraseColor(Color.YELLOW);
        mMarker = mMap.addMarker(new MarkerOptions()
        .position(NEWARK2)
        .title("Brisbane")
        .snippet("Population: 2,074,200")
        .icon(BitmapDescriptorFactory.fromBitmap(mBitmap)));
        mTransparencyBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mGroundOverlay != null) {
            mGroundOverlay.setTransparency((float) progress / (float) TRANSPARENCY_MAX);
        }
    }
}
