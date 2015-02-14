package pckgProjectComplete.com;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class show extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show);
		Button b1 = (Button) findViewById(R.id.button1);
		Button b2 = (Button) findViewById(R.id.button2);
		Button b3 = (Button) findViewById(R.id.button3);
		Button b4 = (Button) findViewById(R.id.button4);
		Button b5 = (Button) findViewById(R.id.button5);

		TextView lname = (TextView) findViewById(R.id.tname);

		Intent i = getIntent();
		Bundle bun = i.getExtras();
		String name = bun.getString("name");
		// String email=bun.getString("email");
		// String location=bun.getString("location");
		// name1.setText(name);
		lname.setText("Welcome " + name);

		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(show.this, AddTransaction.class);
				
				startActivity(i);
				Toast.makeText(getBaseContext(),"You Have Mark Entry"
						 , Toast.LENGTH_LONG).show();
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});
		b2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(show.this, CashBook.class);
				startActivity(i);
				 Toast.makeText(getBaseContext(),"You Have Cash Book"
				 , Toast.LENGTH_LONG).show();

			}

		});
		b3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(show.this, Statistics.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});
		b4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(show.this, Tools.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});
		b5.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(show.this, ProjectCompleteActivity.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});

	}
}