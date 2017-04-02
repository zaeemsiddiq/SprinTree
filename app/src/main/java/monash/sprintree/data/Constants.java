package monash.sprintree.data;

import android.location.Location;

import monash.sprintree.fragments.GMapFragment;
import monash.sprintree.fragments.HistoryFragment;

/**
 * Created by Zaeem on 3/28/2017.
 */

public class Constants {

    /* Fragment instances */
    public static GMapFragment mapFragment;
    public static HistoryFragment historyFragment;

    public static int FRAGMENT_MAP = 0; // used to map tab positions
    public static int FRAGMENT_HISTORY = 1;

    //public static int TOTAL_TREES = 66949;
    public static int TOTAL_TREES = 3000;
    public static int FIREBASE_PAGE_SIZE = 1000;
    public static String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_COARSE_LOCATION"
    };

    /* MAP SETTINGS */
    public static int MAP_ZOOM = 150;
    public static Location LAST_LOCATION;
}
