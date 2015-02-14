package pckgProjectComplete.com;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class ExitHere extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Button b = (Button) findViewById(R.id.button5);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(ExitHere.this, ProjectCompleteActivity.class);
				startActivity(i);

			}
		});
	}
}