package test.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class TestActivity extends Activity {
	ListView listname;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        listname = (ListView)findViewById(R.id.listView1);
        listname.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1));
        listname.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> a, View v, int position,
					long arg3) {
				// TODO Auto-generated method stub
			if(position==0)
			{
				
				Intent i= new Intent(MyList.this,pragati.class);
				startActivity(i);
				
				
				
			}
				
				
			}
		
        
        });
        
        
    }
}