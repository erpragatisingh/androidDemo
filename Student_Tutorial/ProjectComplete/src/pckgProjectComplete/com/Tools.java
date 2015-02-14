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

public class Tools extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tools);
		Button b1 = (Button) findViewById(R.id.button1);
		Button b2 = (Button) findViewById(R.id.button2);
		Button b3 = (Button) findViewById(R.id.button3);
		/*
		 * TextView lname=(TextView)findViewById(R.id.tname);
		 * 
		 * Intent ep=getIntent(); Bundle bun=ep.getExtras(); String
		 * name=bun.getString("name"); // String email=bun.getString("email");
		 * // String location=bun.getString("location"); // name1.setText(name);
		 * //lname.setText("Welcome " + name);
		 */
		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(Tools.this, InterestCalc.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});

		b2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(Tools.this, PlanExpense.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});
		b3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(Tools.this, CurrencyExchange.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});
	}
}