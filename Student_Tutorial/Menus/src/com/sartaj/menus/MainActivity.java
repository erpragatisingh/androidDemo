package com.sartaj.menus;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
/** Called when the activity is first created. */
@Override
public void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_main);
}
@Override
public boolean onCreateOptionsMenu(Menu menu) {
super.onCreateOptionsMenu(menu);
CreateMenu(menu);
return true;
}
@Override
public boolean onOptionsItemSelected(MenuItem item)
{
return MenuChoice(item);
}
private void CreateMenu(Menu menu)
{
//...
}
private boolean MenuChoice(MenuItem item)
{
	return false;
//...
}
}

