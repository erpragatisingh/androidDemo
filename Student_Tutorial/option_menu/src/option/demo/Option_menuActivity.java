package option.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class Option_menuActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);       
    }

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater=getMenuInflater();
		inflater.inflate(R.menu.menuitem, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.search:
			Toast.makeText(getBaseContext(), "Search", Toast.LENGTH_SHORT).show();
			break;
		case R.id.add:
			Toast.makeText(getBaseContext(), "ADD", Toast.LENGTH_SHORT).show();

			break;
			
		case R.id.edit:
			Toast.makeText(getBaseContext(), "Edit", Toast.LENGTH_SHORT).show();

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}