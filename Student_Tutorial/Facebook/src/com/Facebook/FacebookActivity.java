package com.Facebook;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class FacebookActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        WebView wv=(WebView) findViewById(R.id.web);
        
        wv.loadUrl("http://localhost/Mobile%20Jquery%20demos");
       // wv.loadUrl("http://facebook.com");

    }
}