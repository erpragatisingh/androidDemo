package pckgProjectComplete.com;

import pckgProjectComplete.com.User;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	TextView tv1, tv2, tv3;
	EditText edt1, edt2;
	Button btn, sup;
	int count = 0;
	Intent intent = null;
	static String s1, s2;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mylogin);
		// tv1 = (TextView)findViewById(R.id.textView1);
		tv2 = (TextView) findViewById(R.id.textView2);
		tv3 = (TextView) findViewById(R.id.textView3);
		edt1 = (EditText) findViewById(R.id.txtUserName);
		edt2 = (EditText) findViewById(R.id.txtPassword);
		btn = (Button) findViewById(R.id.buttonSignIn);
		//sup = (Button) findViewById(R.id.signup);
		Animation anm = new TranslateAnimation(0, 0, 100, 0);
		anm.setDuration(5000);
		anm.setRepeatCount(-1);
		tv2.setAnimation(anm);
	}

	public void login(View v) {
		LoginUserAuthentication l_user = new LoginUserAuthentication();
		l_user.setUserDetails((edt1.getText()).toString(),
				(edt2.getText()).toString());

		if (!l_user.checkLogin()) {
			count++;
			if (count == 1) {
				tv3.setText("LOGIN FAILED " + count + " TIME");
			} else {
				tv3.setText("LOGIN FAILED " + count + " TIMES");
			}
			edt1.setText("");
			edt2.setText("");
			if (count > 2) {
				LoginActivity.this.finish();
			}
			return;

		}
		tv3.setText("welcome in "
				+ ((User) l_user.getCurrentUser()).getUserName());

		intent = new Intent(this, ProjectCompleteActivity.class);

		startActivity(intent);
		// LoginActivity.this.finish();
	}

	public void afterClickOnText(View v) {
		File file = new File("E://accenture_placement_papers.pdf");
		Uri uri = Uri.fromFile(file);
		intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "Application/pdf");
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, "No Supported Software Found",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		finish();
	}

}