package com.coreservlets.layoutexercises;

import android.app.Activity;

import android.os.Bundle;


/** Exercise on RelativeLayout.
 *  <p>
 *  From <a href="http://www.coreservlets.com/android-tutorial/">
 *  the coreservlets.com Android programming tutorial series</a>.
 */

public class RelativeLayoutsActivity extends Activity {
    /** Initializes the app when it is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.relative_layouts);
    }
}