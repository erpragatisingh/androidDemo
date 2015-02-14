package com.deepesh;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class MyGooglemapActivity extends MapActivity {
    /** Called when the activity is first created. */
	
		    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MapView mapview =(MapView)findViewById(R.id.map_view);
        mapview.setBuiltInZoomControls(true);
     //   mapview.setSatellite(true);
       //  mapview.setStreetView(true);
   mapview.setTraffic(true);
        
        
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return true;
	}
}