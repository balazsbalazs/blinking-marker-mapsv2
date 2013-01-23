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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.moresbycoffee.android.mapv2.BlinkingMarker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

/**
 * This shows how to create a simple activity with a map and a marker on the map.
 * <p>
 * Notice how we deal with the possibility that the Google Play services APK is not
 * installed/enabled/updated on a user's device.
 */
public class BasicMapActivity extends android.support.v4.app.FragmentActivity {
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_demo);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.mapicon_taxi);
        marker = new BlinkingMarker(bitmap, mMap, 24, 2500);
        marker.addToMap(new LatLng(lat, lng));
        marker.startBlinking();
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    lat += 0.0001;
                    lng += 0.0001;
                    marker.moveMarker(new LatLng(lat, lng), true);
                    SystemClock.sleep(300);
                }
            }
            
        }).start();

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
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }
    private static int FPS = 24;
    List<Marker> markers;
    List<Bitmap> bitmaps;
    double lat = 43.649887;
    double lng = -79.387755;
    BlinkingMarker marker;
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 13f));
//        
//        markers = new ArrayList<Marker>();
//        bitmaps = new ArrayList<Bitmap>();
//        for (int i=0; i<FPS; i++) {
//            bitmaps.add(adjustOpacity(bitmap, 255/FPS*i));
//            markers.add(addMarker(bitmaps.get(i)));
//        }
//        startAnimation();
    }

    private void startAnimation() {
        new Thread(new Runnable() {

            private int prev = FPS-1;
            private int pointer = FPS-1;
            private int direction = -1;
            private int counter = 0;

            @Override
            public void run() {
                while (true) {
                    if (pointer == FPS-1) {
                        direction = -1;
                    } else if (pointer == 0) {
                        direction = 1;
                    }
                    if (counter > 20000 && pointer == 0) {
                        counter = 0;
                        changeMarkerVisibility(pointer, prev, true);
                    } else {
                        changeMarkerVisibility(pointer, prev, false);
                    }
                    prev = pointer;
                    pointer += direction;
                    SystemClock.sleep(1000/FPS);
                    counter++;
                }
            }
            
        }).start();
    }

    public void changeMarkerVisibility(final int visibleMarker, final int invisibleMarker, final boolean move) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                markers.get(visibleMarker).setVisible(true);
                markers.get(invisibleMarker).setVisible(false);
                if (move)
                    moveMarkers();
            }
            
        });
    }

    private Marker addMarker(Bitmap b1) {
        Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromBitmap(b1)));
        m.setVisible(false);
        return m;
    }

    private Bitmap adjustOpacity(Bitmap bitmap, int opacity) {
        Bitmap mutableBitmap = bitmap.isMutable() ? bitmap : bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int colour = (opacity & 0xFF) << 24;
        canvas.drawColor(colour, PorterDuff.Mode.DST_IN);
        return mutableBitmap;
    }

    private void moveMarkers() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                lat += 0.0001;
                lng += 0.0001;
                mMap.clear();
                markers.clear();
                for (int i=0; i<FPS; i++) {
                    markers.add(addMarker(bitmaps.get(i)));
                }
            }
            
        });
    }
    
    public void onPause() {
        super.onPause();
        marker.removeMarker();
    }
    
}
