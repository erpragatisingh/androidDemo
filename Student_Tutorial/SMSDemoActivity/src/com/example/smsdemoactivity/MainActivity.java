package com.example.smsdemoactivity;

import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.telephony.PhoneNumberUtils;
import android.telephony.gsm.SmsManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	private EditText number;
	private EditText message;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        number=(EditText)findViewById(R.id.editText1);
        message=(EditText)findViewById(R.id.editText2);
        Button button =(Button)findViewById(R.id.button1);
        button.setOnClickListener(new OnClickListener(
        		) {
			
			public void onClick(View v) {
				
				SmsManager smsManager =SmsManager.getDefault();
				
			final PendingIntent seIntent=PendingIntent.getBroadcast(MainActivity.this, 0, new Intent, flags);	 
			String  dest = number.getText().toString();
			if(PhoneNumberUtils.isWellFormedSmsAddress(dest))
			{
				smsManager.sendTextMessage(dest, null, message.getText().toString(), SentI, null);
				
				Toast.makeText(MainActivity.this, "sms sent", Toast.LENGTH_LONG);
			}
				
			}
		});
           
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}

























