package monash.sprintree.utils;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by Zaeem on 3/27/2017.
 */

public class Utils {
    private void fullScreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
