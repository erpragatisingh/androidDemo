package com.coreservlets.layoutexercises;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

/** Solutions to some simple exercises that use Layouts.
 *  <p>
 *  From <a href="http://www.coreservlets.com/android-tutorial/">
 *  the coreservlets.com Android programming tutorial series</a>.
 */

public class LayoutsExercisesInitialActivity extends Activity {
    /** Initializes the app when it is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    /** Switches to the LinearLayoutsActivity when the associated button is clicked. */
    
    public void showLinearLayouts(View clickedButton) {
        ActivityUtils.goToActivity(this, LinearLayoutsActivity.class);
    }
      
          /** Switches to the RelativeLayoutsActivity when the associated button is clicked. */
    
    public void showRelativeLayouts(View clickedButton) {
        ActivityUtils.goToActivity(this, RelativeLayoutsActivity.class);
    }
}