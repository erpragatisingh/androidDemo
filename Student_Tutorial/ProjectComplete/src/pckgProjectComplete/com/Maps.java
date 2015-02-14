package pckgProjectComplete.com;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.maps.MapActivity;
import android.os.Bundle;

public class Maps extends MapActivity {
/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.maps);
}
@Override
protected boolean isRouteDisplayed() {
// TODO Auto-generated method stub
return false;
}
}