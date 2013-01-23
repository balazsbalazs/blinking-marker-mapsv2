blinking-marker-mapsv2
======================

This is a workaround for creating a blinking marker in Google Maps v2 for Android.

I've raised an issue with the maps team and hopefully they're already working on this:
http://code.google.com/p/gmaps-api-issues/issues/detail?id=4768&thanks=4768&ts=1357300845

Idea
-----

The idea behind a workaround is to create a series of markers with the same icon but different opacity defined. After that show periodically a marker with different opacity on the map. You can either add/remove the appropriate marker but that results in a lot of GC. The "better" solution is to add all of the markers at once and only change their visibility. This solution consumes more memory but not that much continuous allocations.


Usage
------

Add the marker to the map and initialize it.

	@Override
	public void onCreate(Bundle savedInstanceState) {
	…
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mapicon_taxi);
        marker = new BlinkingMarker(bitmap, mMap, fps, duration);
        marker.addToMap(new LatLng(14.6, 54.6));
        marker.startBlinking();
	}


Move the marker:

	…
	marker.move(new LatLng(14.7, 54.6));
	…
	
Stop the blinking on onPause()

	@Override
	public void onPause() {
		...
		marker.stopBlinking();
	}

Sample
-----

There is a sample activity in the source which illustrates all the capabilities of the marker.

In order to set it up you need to:

* change the google maps v2 key in res/values/maps_key.xml
* add the Google Play Services library project to the project


