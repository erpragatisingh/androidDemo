package pckgProjectComplete.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class InterestCalc extends Activity implements OnClickListener {

	EditText Principal;

	EditText Rate;
	EditText Freq;
	EditText Years;
	EditText ans;
	float p, r, n, f, a;
	String s;
	Button Calculate;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.interestcalc);

	/*	TextView lname = (TextView) findViewById(R.id.acname);

		Intent i = getIntent();
		Bundle bun = i.getExtras();
		String name = bun.getString("name");
		// String email=bun.getString("email");
		// String location=bun.getString("location");
		// name1.setText(name);
		lname.setText("Wlecome " + name);
*/
		Principal = (EditText) findViewById(R.id.editText1);
		Rate = (EditText) findViewById(R.id.editText2);
		Freq = (EditText) findViewById(R.id.editText5);
		Years = (EditText) findViewById(R.id.editText3);
		ans = (EditText) findViewById(R.id.editText4);

		Calculate = (Button) findViewById(R.id.button1);
		Calculate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				p = Float.parseFloat(InterestCalc.this.Principal.getText()
						.toString());
				r = Float.parseFloat(InterestCalc.this.Rate.getText()
						.toString());
				n = Float.parseFloat(InterestCalc.this.Years.getText()
						.toString());
				f = Float.parseFloat(InterestCalc.this.Freq.getText()
						.toString());
				r = (float) (r / (f * 100));
				a = (float) (p * Math.pow((1 + r), (f * n)));
				s = String.valueOf(a);

				ans.setText(s);
			}

		});
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

	}
}