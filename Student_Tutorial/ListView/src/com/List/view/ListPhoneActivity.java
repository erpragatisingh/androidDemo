package com.List.view;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class ListPhoneActivity extends ListActivity {
    /* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Toast.makeText(this, "You have selected a Phone" +cities[position], Toast.LENGTH_SHORT).show();
		
		
		
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
         
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item,cities));
    }
    String[]cities={"Nokia","sumsung","LG","Motorala","Nokia","Sumgung","Nokia","DEL"};
}
