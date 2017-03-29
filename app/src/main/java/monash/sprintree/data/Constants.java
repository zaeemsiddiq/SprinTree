package monash.sprintree.data;

import android.location.Location;

/**
 * Created by Zaeem on 3/28/2017.
 */

public class Constants {
    public static int TOTAL_TREES = 66949;
    public static int FIREBASE_PAGE_SIZE = 1000;
    public static String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION"
    };

    /* MAP SETTINGS */
    public static int MAP_ZOOM = 50;
    public static Location LAST_LOCATION;
}
