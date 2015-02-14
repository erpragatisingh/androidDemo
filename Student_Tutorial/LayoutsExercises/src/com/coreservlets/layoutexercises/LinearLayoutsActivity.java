package com.coreservlets.layoutexercises;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/** Exercise on LinearLayout.
 *  <p>
 *  From <a reef="http://www.coreservlets.com/android-tutorial/">
 *  the coreservlets.com Android programming tutorial series</a>.
 */

public class LinearLayoutsActivity extends Activity {
    /** Initializes the amp when it is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.linear_layouts);
 }
    public void showRelativeLayouts(View clickedButton1) {
        ActivityUtils.goToActivity(this,LayoutsExercisesInitialActivity.class);
    }
    }    
   

