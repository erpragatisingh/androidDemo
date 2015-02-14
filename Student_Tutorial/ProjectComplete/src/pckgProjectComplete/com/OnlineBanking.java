package pckgProjectComplete.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class OnlineBanking extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.onlinebanking);
		Button b = (Button) findViewById(R.id.button1);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(OnlineBanking.this,
						ProjectCompleteActivity.class);
				startActivity(i);

			}
		});

	}

	// RadioGroup rGroup = (RadioGroup) findViewById(R.id.radioGroup1);

	/*
	 * OnClickListener radio_listener = new OnClickListener() { public void
	 * onClick(View v) { onRadioButtonClick(v); } };
	 * 
	 * /*RadioButton radio0 = new RadioButton(this);
	 * button1.setText(R.string.rad_option1); button1.setTextColor(Color.RED);
	 * button1.setOnClickListener(radio_listener); rGroup3.addView(button1);
	 * radio0.
	 * 
	 * RadioButton button2 = new RadioButton(this);
	 * button2.setText(R.string.rad_option2); button2.setTextColor(Color.GREEN);
	 * button2.setOnClickListener(radio_listener); rGroup3.addView(button2);
	 * 
	 * RadioButton button3 = new RadioButton(this);
	 * button3.setText(R.string.rad_option3); button3.setTextColor(Color.BLUE);
	 * button3.setOnClickListener(radio_listener); rGroup3.addView(button3);
	 * rGroup3.check(button1.getId());
	 */
}
