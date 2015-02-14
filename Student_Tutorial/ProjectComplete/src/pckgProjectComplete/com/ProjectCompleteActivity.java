package pckgProjectComplete.com;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class ProjectCompleteActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button b1 = (Button) findViewById(R.id.button1);
		Button b2 = (Button) findViewById(R.id.button2);
		Button b3 = (Button) findViewById(R.id.button3);
		Button b4 = (Button) findViewById(R.id.button4);

		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(ProjectCompleteActivity.this, ListAccounts.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});
		b2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(ProjectCompleteActivity.this,
						OnlineBanking.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});
		b3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(ProjectCompleteActivity.this, PlanExpense.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});
		b4.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent i = new Intent(ProjectCompleteActivity.this, Exit.class);
				startActivity(i);
				// Toast.makeText(getBaseContext(),"You Have Clicked This Button"
				// , Toast.LENGTH_LONG).show();

			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Context con = getApplicationContext();
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.item1:
			Toast.makeText(getBaseContext(), "Account-Related",
					Toast.LENGTH_SHORT).show();
			Intent i1 = new Intent(con, ListAccounts.class);
			startActivity(i1);
			break;
		case R.id.item2:
			Toast.makeText(getBaseContext(), "Online Banking",
					Toast.LENGTH_SHORT).show();
			Intent i2 = new Intent(con, OnlineBanking.class);
			startActivity(i2);
			break;

		case R.id.item3:
			Toast.makeText(getBaseContext(), "Plan Expense", Toast.LENGTH_SHORT)
					.show();
			Intent i3 = new Intent(con, PlanExpense.class);
			startActivity(i3);
			break;
		case R.id.item4:
			Toast.makeText(getBaseContext(), "Exit", Toast.LENGTH_SHORT).show();
			Intent i4 = new Intent(con, Exit.class);
			startActivity(i4);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}