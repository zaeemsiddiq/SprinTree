package monash.sprintree.data;

import android.location.Location;

import java.util.List;

import monash.sprintree.fragments.GMapFragment;
import monash.sprintree.fragments.HistoryFragment;

/**
 * Created by Zaeem on 3/28/2017.
 */

public class Constants {

    public static int REQUEST_EXIT = 0;

    //public static int TOTAL_TREES = 66949;
    public static int TOTAL_TREES = 3000;
    public static int FIREBASE_PAGE_SIZE = 3000;
    public static String[] permissions = {
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.ACCESS_COARSE_LOCATION"
    };

    /* MAP SETTINGS */
    public static float MAP_ZOOM = 17.0f;
    public static Location LAST_LOCATION;
    public static int WIKIPEDIA_IMAGE_SIZE = 200; //px

    /* Data Objects */
    public static List<Tree> trees;
    public static List<Tree> commonTrees;

    public static final int FRAGMENT_BUTTON_START     = 1;
    public static final int FRAGMENT_BUTTON_PAUSE     = 2;
    public static final int FRAGMENT_BUTTON_RESUME    = 3;
    public static final int FRAGMENT_BUTTON_STOP      = 4;

    /* Journey Constants */
    public static int MILESTONE_DISTANCE = 100; // load nearest trees after x meters
    public static int TREES_RADIUS = 600;   // meters to load trees
    public static float NEAREST_TREE_DISTANCE = (float) 10.00; // distance identifier to get the nearest tree
    public static int TREE_NORMAL_SCORE = 10;   // tree score
}
