package com.ngma;

import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class GooglemapActivity extends MapActivity {
	
	MapView mapView;
    /** Called when the activity is first created. */
   
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mapView = (MapView) findViewById(R.id.mv1); 
        mapView.setBuiltInZoomControls(true); 
        mapView.setSatellite(true); 
        
        MapController mapControl = mapView.getController(); 
        double lat = 51.50773; 
        double lng = -0.16582; 
        GeoPoint gPoint = 
        		new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6)); 
        mapControl.animateTo(gPoint); 
        mapControl.setZoom(17); 
        mapView.invalidate(); 


    }

 
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}