package com.sartaj.receivingvalue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Activity1Activity extends Activity {
    /** Called when the activity is first created. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  super.onActivityResult(requestCode, resultCode, data);
  if(requestCode==1){
	  Bundle bundle=data.getExtras();
	  int val=bundle.getInt("paasvalue");
	  TextView tv=(TextView)findViewById(R.id.textView1);
	  tv.append(String.valueOf(val));
  }
    }
		// TODO Auto-generated method stub)
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button button=(Button)findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			Intent intent=new Intent(Activity1Activity.this,secondActivity.class);
			startActivityForResult(intent,1);
			}
		});
        
        
    }
}
