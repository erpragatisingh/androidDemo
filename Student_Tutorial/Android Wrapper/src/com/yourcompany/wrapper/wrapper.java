package com.yourcompany.wrapper;



import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Display;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class wrapper extends Activity {
	WebView webview;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		webview = (WebView) findViewById(R.id.webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setUseWideViewPort(true);
		webview.getSettings().setBuiltInZoomControls(true);
		webview.setVerticalScrollBarEnabled(false);
		webview.setHorizontalScrollBarEnabled(false);
		Display display = getWindowManager().getDefaultDisplay();
		double ratio = display.getWidth() / 9.7;
		webview.setInitialScale((int) ratio);
		webview.loadUrl("file:///android_asset/index.htm?androidapp");

		final Context myApp = this;

		webview.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final android.webkit.JsResult result) {
				new AlertDialog.Builder(myApp)
						.setTitle("JavaScript Alert")
						.setMessage(message)
						.setPositiveButton(android.R.string.ok,
								new AlertDialog.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										result.confirm();
									}
								})

						.setCancelable(false).create().show();

				return true;
			};
		});
	}
}
