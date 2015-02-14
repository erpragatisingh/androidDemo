package pckgProjectComplete.com;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddAccount extends Activity {
	private EditText textBox;
    public String s;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addaccount);
		
		/*Button b1 = (Button) findViewById(R.id.button1);
		b1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 Intent i = new Intent(AddAccount.this,Maps.class);
				 startActivity(i);
				 Toast.makeText(getBaseContext(),"You are accessing the Tracking Feature", Toast.LENGTH_LONG).show();

			}

		});
*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return Utils.inflateMenu(this, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return Utils.handleMenuOption(this, item);
	}

	public void addAccount(View v) {
		// get access to views

		EditText editAcno = (EditText) this.findViewById(R.id.editAcno);
		if( editAcno.getText().toString().length() == 0 )
			editAcno.setError( "First name is required!" );
		
		EditText editCno = (EditText) this.findViewById(R.id.editCno);
		if( editCno.getText().toString().length() == 0 )
			editCno.setError( "First name is required!" );
		
		EditText editHolders = (EditText) this.findViewById(R.id.editHolders);
		if( editHolders.getText().toString().length() == 0 )
			editHolders.setError( "First name is required!" );
		
		EditText editBankName = (EditText) this.findViewById(R.id.editBankName);
		if( editBankName.getText().toString().length() == 0 )
			editBankName.setError( "First name is required!" );
		
		EditText editBranchName = (EditText) this.findViewById(R.id.editBranchName);
		if( editBranchName.getText().toString().length() == 0 )
			editBranchName.setError( "First name is required!" );
		
		EditText editAddress = (EditText) this.findViewById(R.id.editAddress);
		if( editAddress.getText().toString().length() == 0 )
			editAddress.setError( "First name is required!" );
		// EditText editIFSC = (EditText) this.findViewById(R.id.editIFSC);
		// EditText editMICR = (EditText) this.findViewById(R.id.editMICR);
		
		EditText editBalance = (EditText) this.findViewById(R.id.editBalance);
		if( editBalance.getText().toString().length() == 0 )
			editBalance.setError( "First name is required!" );
		
		EditText editRemarks = (EditText) this.findViewById(R.id.editRemarks);
		if(  editRemarks.getText().toString().length() == 0 )
			 editRemarks.setError( "First name is required!" );
		
		
		try {
			DBHelper dbhelper = new DBHelper(this);
			SQLiteDatabase db = dbhelper.getWritableDatabase();
			Log.d("Account", "Got Writable database");
			// execute insert command

			ContentValues values = new ContentValues();

			String p = String.valueOf(editAcno.getText().toString());
			String q = String.valueOf(editCno.getText().toString());
			String r = String.valueOf(editHolders.getText().toString());
			String s = String.valueOf(editBankName.getText().toString());
			String t = String.valueOf(editBranchName.getText().toString());
			String u = String.valueOf(editAddress.getText().toString());
			String w = String.valueOf(editBalance.getText().toString());
			String x = String.valueOf(editRemarks.getText().toString());

			values.put(Database.ACCOUNTS_ACNO, p);
			values.put(Database.ACCOUNTS_CNO, q);
			values.put(Database.ACCOUNTS_HOLDERS, r);
			values.put(Database.ACCOUNTS_BANK, s);
			values.put(Database.ACCOUNTS_BRANCH, t);
			values.put(Database.ACCOUNTS_ADDRESS, u);
			// values.put( Database.ACCOUNTS_IFSC,
			// editIFSC.getText().toString());
			// values.put( Database.ACCOUNTS_MICR,
			// editMICR.getText().toString());
			values.put(Database.ACCOUNTS_BALANCE, w);
			values.put(Database.ACCOUNTS_REMARKS, x);

			long rows = db.insert(Database.ACCOUNTS_TABLE_NAME, null, values);
			db.close();

			/*
			 * if(!validateInputInformation(values.put())){ return; }
			 */
			if ((p == "" || q == "" || r == "" || s == "" || t == "" || u == ""
					|| w == "" || x == "")) {
				Toast.makeText(getApplicationContext(),
						"Please fill all the information", Toast.LENGTH_SHORT)
						.show();
			}
			/*
			 * else { sendSMS(emNo,ename);
			 * insert(eid,ename,eemail,emNo,edob,eremark); }
			 */

			if (rows > 0) {
				Toast.makeText(this, "Added Account Successfully!",
						Toast.LENGTH_LONG).show();
				this.finish();
			} else
				Toast.makeText(this, "Sorry! Could not add account!",
						Toast.LENGTH_LONG).show();

		} catch (Exception ex) {
			Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
		}

	}

}
