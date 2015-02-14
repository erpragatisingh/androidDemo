package com.ashish;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SmsActivity extends Activity {
	EditText messageText;
	Button sendButton;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        messageText = (EditText) findViewById(R.id.editText1);
        sendButton = (Button) findViewById(R.id.button1);
        sendButton.setOnClickListener(new OnClickListener() {
        	
			public void onClick(View v) {
        	String message = messageText.getText().toString();
        	
			SmsManager sms = SmsManager.getDefault();
        	sms.sendTextMessage("5556", null, message, null, null);
        	}
        	});
        	
    }
}
