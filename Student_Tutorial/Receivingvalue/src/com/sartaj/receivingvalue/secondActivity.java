package com.sartaj.receivingvalue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class secondActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	
	    // TODO Auto-generated method stub
	    setContentView(R.layout.second);
	    Button button=(Button)findViewById(R.id.button2);
	    button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			Intent intent=new Intent();
			intent.putExtra("paasvalue", 77);
			setResult(RESULT_OK,intent);
			finish();
			}
		});
	}

}
