package com.sartaj.animalsound;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */
	private MediaPlayer mp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		View button1 = findViewById(R.id.imageButton1);
		View button2 = findViewById(R.id.imageButton2);
		View button3 = findViewById(R.id.imageButton3);
		button1.setOnClickListener(this);
		button2.setOnClickListener(this);
		button3.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		 if(mp != null) mp.release();
		switch (v.getId()) {

		// The cat button
		case R.id.imageButton1:
			mp = MediaPlayer.create(this, R.raw.cat);
			break;

		// The duck button
		case R.id.imageButton2:
		
			mp = MediaPlayer.create(this, R.raw.duck);
			break;

		// The sheep button
		case R.id.imageButton3:
	
			mp = MediaPlayer.create(this, R.raw.sheep);
			break;

		}
	
		mp.start();

	}
	
	    
}
