package monash.sprintree.activities;

import android.Manifest;
import android.animation.Animator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompatBase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import br.com.goncalves.pugnotification.notification.PugNotification;
import cn.pedant.SweetAlert.SweetAlertDialog;
import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Journey;
import monash.sprintree.data.JourneyPath;
import monash.sprintree.data.JourneyTree;
import monash.sprintree.data.Marker;
import monash.sprintree.data.Tree;
import monash.sprintree.fragments.FragmentListener;
import monash.sprintree.fragments.GMapFragment;
import monash.sprintree.fragments.HistoryFragment;
import monash.sprintree.fragments.MyPlantFragment;
import monash.sprintree.service.TreeService;
import monash.sprintree.utils.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapsActivity extends FragmentActivity implements LocationListener, FragmentListener {


    boolean isActivityDataLoaded = false;
    /*
    Fragment objects
     */
    private GMapFragment mapFragment;
    private HistoryFragment historyFragment;
    private MyPlantFragment myPlantFragment;

    public static int FRAGMENT_MAP = 0; // used to map tab positions
    public static int FRAGMENT_HISTORY = 1;

    /*
    View Objects
     */
    private TabLayout tabLayoutDashboard;
    static final int REQUEST_PERMISSION_CODE = 100;
    Fragment currentFragment;
    LocationManager locationManager;
    private String provider;

    /*
    Data objects
     */
    List<Marker> nonUniqueMarkers;
    List<Marker> uniqueMarkers;
    List<Marker> unlockedMarkers;

    /*
    Journey Objects
     */
    boolean JOURNEY_STARTED;
    int journeyScore;
    long journeyDistance;
    int journeyHours;
    int journeyMins;
    int journeySecs;
    List<JourneyPath> journeyPathList;
    List<JourneyTree> journeyTreeList;
    List<Polyline> polyLines;

    private Location lastLocationMilestone;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        //Utils.fullScreen(MapsActivity.this);
        setContentView(R.layout.activity_maps);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            handlePermissions();
        } else {
            initiateLocationManager();
            loadData();
            initLayout();
        }
    }

    private void loadData() {
        Constants.IS_APPLICATION_MINIMIZED = false;
        JOURNEY_STARTED = false;
        journeyScore = 0;
        journeyDistance = 0;
        journeyHours = 0;
        journeyMins = 0;
        journeySecs = 0;
        journeyPathList = new ArrayList<>();
        journeyTreeList = new ArrayList<>();

        uniqueMarkers = new ArrayList<>();
        nonUniqueMarkers = new ArrayList<>();
        unlockedMarkers = new ArrayList<>();
        polyLines = new ArrayList<Polyline>();
        lastLocationMilestone = null;
        loadTrees();
    }

    private void loadTrees() {
        uniqueMarkers.clear();
        nonUniqueMarkers.clear();
        unlockedMarkers.clear();
        for (Tree tree : radiusBoundedTrees(Constants.TREES_RADIUS)) {
            if ((tree.genus.equals("Ulmus")) || (tree.genus.equals("Eucalyptus")) || (tree.genus.equals("Platanus"))
                    || (tree.genus.equals("Corymbia")) || (tree.genus.equals("Angophora")) || (tree.genus.equals("Allocasuarina"))
                    || (tree.genus.equals("Acacia")) || (tree.genus.equals("Quercus")) || (tree.genus.equals("Ficus"))
                    || (tree.genus.equals("Melaleuca")) || (tree.genus.equals("Lophostemon")) || (tree.genus.equals("Callistemon"))
                    || (tree.genus.equals("Acer")) || (tree.genus.equals("Casuarina"))
                    ) {
                if (isTreeVisited(tree)) {
                    unlockedMarkers.add(new Marker(new LatLng(tree.latitude, tree.longitude), tree.commonName, tree.genus, R.drawable.tree_visited, tree.comId));
                } else {
                    nonUniqueMarkers.add(new Marker(new LatLng(tree.latitude, tree.longitude), tree.commonName, tree.genus, R.drawable.tree, tree.comId));
                }
            } else {
                if (isTreeVisited(tree)) {
                    unlockedMarkers.add(new Marker(new LatLng(tree.latitude, tree.longitude), tree.commonName, tree.genus, R.drawable.tree_visited, tree.comId));
                } else {
                    uniqueMarkers.add(new Marker(new LatLng(tree.latitude, tree.longitude), tree.commonName, tree.genus, R.drawable.tree_unique, tree.comId));
                }
            }
        }
    }

    // function to get all trees within the specified radius
    private List<Tree> radiusBoundedTrees(int radius) {
        List<Tree> nearestTrees = new ArrayList<>();
        Location myLocation = Constants.LAST_LOCATION;
        if (myLocation != null) {
            for (Tree tree : Constants.trees) {
                float[] results = new float[1];
                Location.distanceBetween(myLocation.getLatitude(), myLocation.getLongitude(), tree.latitude, tree.longitude, results); // in case of 0 previous stop is the starting stop
                if (results[0] < radius) {
                    nearestTrees.add(tree);
                }
            }
        }
        if (nearestTrees.size() == 0) {
            if (currentFragment == mapFragment) {
                Toast.makeText(this, "No Trees Nearby, Try in CBD", Toast.LENGTH_SHORT).show();
            }
        }
        return nearestTrees;
    }

    private void initLayout() {
        initiateTabsLayout();
        mapFragment = GMapFragment.newInstance(MapsActivity.this, nonUniqueMarkers, uniqueMarkers, unlockedMarkers);
        historyFragment = HistoryFragment.newInstance(MapsActivity.this);
        myPlantFragment = MyPlantFragment.newInstance(this);
        Bundle extras = getIntent().getExtras();
        int selectTabId = 0;
        if (extras != null) {
            findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
            findViewById(R.id.mainFrame).setVisibility(View.VISIBLE);
            selectTabId = extras.getInt("showTab");

        }
        selectTab(selectTabId);
        isActivityDataLoaded = true;
    }

    private void initiateTabsLayout() { // adding the tabs dynamically
        tabLayoutDashboard = (TabLayout) findViewById(R.id.mainTabs);
        tabLayoutDashboard.addTab(tabLayoutDashboard.newTab().setText("Map").setIcon(R.drawable.mapico)); //0
        tabLayoutDashboard.addTab(tabLayoutDashboard.newTab().setText("History").setIcon(R.drawable.historyico)); //1
        tabLayoutDashboard.addTab(tabLayoutDashboard.newTab().setText("My Forest").setIcon(R.drawable.treeico)); //2

        tabLayoutDashboard.getTabAt(0).getIcon().setColorFilter(Color.parseColor("#A1D700"), PorterDuff.Mode.SRC_IN);
        tabLayoutDashboard.getTabAt(1).getIcon().setColorFilter(Color.parseColor("#A1D700"), PorterDuff.Mode.SRC_IN);
        tabLayoutDashboard.getTabAt(2).getIcon().setColorFilter(Color.parseColor("#A1D700"), PorterDuff.Mode.SRC_IN);

        tabLayoutDashboard.setTabMode(TabLayout.MODE_FIXED);
        changeTabsFont(tabLayoutDashboard);

        tabLayoutDashboard.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    if (mapFragment != null) {
                        // hide the current fragment first
                        hideFragment(currentFragment);
                        // add/show the fragment
                        showFragment(mapFragment);
                    }
                }
                if (tab.getPosition() == 1) {
                    historyFragment = HistoryFragment.newInstance(MapsActivity.this);
                    hideFragment(currentFragment);
                    showFragment(historyFragment);
                }
                if (tab.getPosition() == 2) {
                    myPlantFragment = MyPlantFragment.newInstance(MapsActivity.this);
                    hideFragment(currentFragment);
                    showFragment(myPlantFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void initiateLocationManager() {
        // Get the location manager
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Allow permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        if (locationManager == null) {
            // Get the location manager
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2000,
                    1, this);
        }
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Allow permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            Constants.LAST_LOCATION = location;
            onLocationChanged(location);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        if (lastLocationMilestone == null) {
            lastLocationMilestone = location;
        }
        float lat = (float) (location.getLatitude());
        float lng = (float) (location.getLongitude());

        if (JOURNEY_STARTED) {
            addJourneyPathToList(lat, lng);
            calculateAndAddNearestTree(lat, lng);
            journeyDistance += distanceTravelled(lat, lng);
            mapFragment.moveCamera(location);
            drawLineOnMap(location);
            if (Constants.IS_APPLICATION_MINIMIZED) {
                for (Marker marker : uniqueMarkers) {
                    float[] results = new float[1];
                    Location.distanceBetween(lat, lng, marker.getPosition().latitude, marker.getPosition().longitude, results); // in case of 0 previous stop is the starting stop
                    if ( results[0] < Constants.UNIQUE_TREE_NOTIFICATION_DISTANCE ) {
                        generateNotification( "Hang On!!", "There is a unique tree nearby" );
                    }
                }
            }
        }
        Constants.LAST_LOCATION = location;
        if (distanceTravelled((float) lastLocationMilestone.getLatitude(), (float) lastLocationMilestone.getLongitude()) >= Constants.MILESTONE_DISTANCE) {   // load nearest trees
            lastLocationMilestone = location;
            loadTrees();
            mapFragment.reloadTrees(nonUniqueMarkers, uniqueMarkers, unlockedMarkers);
            drawLineOnMap(location);
        }

    }

    private void generateNotification(String title, String message) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.treeico)
                        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                                R.drawable.tree))
                        .setContentTitle(title)
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                        .setContentText(message);

        Intent notificationIntent = getIntent();
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    private float distanceTravelled(float lat, float lng) {
        float[] results = new float[1];
        Location.distanceBetween(lat, lng, Constants.LAST_LOCATION.getLatitude(), Constants.LAST_LOCATION.getLongitude(), results);
        return results[0];
    }

    private void drawLineOnMap(Location location) {
        removePolyLines();
        PolylineOptions polylineOptions = new PolylineOptions().width(5).color(Color.RED);
        ArrayList<LatLng> coordList = new ArrayList<LatLng>();
        for( JourneyPath path : journeyPathList  ) {
            coordList.add( new LatLng(path.latitude, path.longitude));
            polylineOptions.add( new LatLng(path.latitude, path.longitude));
        }
        polyLines.add(mapFragment.addPolylines(polylineOptions));

        //LatLng from = new LatLng(Constants.LAST_LOCATION.getLatitude(), Constants.LAST_LOCATION.getLongitude());
        //LatLng to = new LatLng(location.getLatitude(), location.getLongitude());
        //polyLines.add(mapFragment.addPolyLine(from, to));
    }


    private void removePolyLines() {
        for (Polyline polyline : polyLines) {
            polyline.remove();
        }
        polyLines.clear();
    }

    public void hideTab() {
        mapFragment.makeClockVisible(true);
        tabLayoutDashboard.setVisibility(View.GONE);
    }

    public void showTab() {
        mapFragment.makeClockVisible(false);
        tabLayoutDashboard.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean isTreeVisited(Tree tree) {
        boolean treeVisited = false;
        for (JourneyTree journeyTree : journeyTreeList) {
            if (journeyTree.tree.comId.equals(tree.comId)) {
                treeVisited = true;
                break;
            }
        }
        for (Tree journeyTree : TreeService.getVisitedTrees()) {
            if (journeyTree.comId.equals(tree.comId)) {
                treeVisited = true;
                break;
            }
        }
        return treeVisited;
    }

    @Override
    public void updateTimer(int hrs, int mins, int secs) {
        journeyHours = hrs;
        journeyMins = mins;
        journeySecs = secs;
    }

    private void calculateAndAddNearestTree(float lat, float lng) {
        Marker nearestMarker = null;
        boolean isNearestTreeFound = false;
        Tree nearestTree = null;
        if (mapFragment != null) {
            //float[] results = new float[1]; // initialising a 1d result array to pass into distanceBetween method (source: developers.google.com)
            for (Marker marker : nonUniqueMarkers) {
                float[] results = new float[1];
                Location.distanceBetween(lat, lng, marker.getPosition().latitude, marker.getPosition().longitude, results); // in case of 0 previous stop is the starting stop
                if (results[0] < Constants.NEAREST_TREE_DISTANCE) {
                    nearestTree = TreeService.findTreeByPosition(marker.getPosition());
                    if (!isTreeVisited(nearestTree)) {  // tree not visited before, add tree now
                        isNearestTreeFound = true;
                        nearestMarker = marker;
                        JourneyTree journeyTree = new JourneyTree();
                        journeyTree.tree = nearestTree;
                        journeyTreeList.add(journeyTree);
                        journeyScore += Constants.TREE_NORMAL_SCORE;
                        mapFragment.updateViews(journeyScore);
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Tree Unlocked - " + marker.getTitle(), Snackbar.LENGTH_LONG).show();
                        break;
                    }
                }
            }
            for (Marker marker : uniqueMarkers) {
                float[] results = new float[1];
                Location.distanceBetween(lat, lng, marker.getPosition().latitude, marker.getPosition().longitude, results); // in case of 0 previous stop is the starting stop
                if (results[0] < Constants.NEAREST_TREE_DISTANCE) {
                    nearestTree = TreeService.findTreeByPosition(marker.getPosition());
                    if (!isTreeVisited(nearestTree)) {  // tree not visited before, add tree now
                        isNearestTreeFound = true;
                        nearestMarker = marker;
                        JourneyTree journeyTree = new JourneyTree();
                        journeyTree.tree = nearestTree;
                        journeyTreeList.add(journeyTree);
                        journeyScore += Constants.TREE_NORMAL_SCORE;
                        mapFragment.updateViews(journeyScore);
                        Snackbar.make(getWindow().getDecorView().getRootView(), "Tree Unlocked - " + marker.getTitle(), Snackbar.LENGTH_LONG).show();
                        break;
                    }
                }
            }
            if (isNearestTreeFound) {
                nonUniqueMarkers.remove(nearestMarker);
                uniqueMarkers.remove(nearestMarker);
                unlockedMarkers.add(new Marker(new LatLng(nearestTree.latitude, nearestTree.longitude), nearestTree.commonName, nearestTree.genus, R.drawable.tree_visited, nearestTree.comId));
            }
            mapFragment.reloadTrees(nonUniqueMarkers, uniqueMarkers, unlockedMarkers);
        }
    }

    private void addJourneyPathToList(float lat, float lng) {
        JourneyPath journeyPath = new JourneyPath();
        journeyPath.latitude = lat;
        journeyPath.longitude = lng;
        journeyPath.timestamp = Utils.getCurrentTimeStamp();
        journeyPathList.add(journeyPath);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private boolean isGpsConnected() {
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showGpsDialog() {
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
        dialog.setTitleText("Please turn on Location services");
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Turn On");
        dialog.showCancelButton(true);
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 3);
                sweetAlertDialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first

        // The activity is either being restarted or started for the first time
        // so this is where we should make sure that GPS is enabled
        if (!isGpsConnected()) {
            // Create a dialog here that requests the user to enable GPS, and use an intent
            // with the android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS action
            // to take the user to the Settings screen to enable GPS when they click "OK"
            showGpsDialog();
        } else {

        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();  // Always call the superclass method first
        // Activity being restarted from stopped state
    }

    @Override
    protected void onUserLeaveHint() {
        if (isActivityDataLoaded) {
            Constants.IS_APPLICATION_MINIMIZED = true;  // app is minimised
        }
        Log.d("onUserLeaveHint", "Home button pressed");
        super.onUserLeaveHint();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (locationManager == null) {
            // Get the location manager
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    2000,
                    1, this);
        }
        Constants.IS_APPLICATION_MINIMIZED = false;
        Log.d("***", "Resumed");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3) {

        } else {
            historyFragment = HistoryFragment.newInstance(MapsActivity.this);
            selectTab(FRAGMENT_HISTORY);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    private void handlePermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {  // current version is Marshmallow, which requires permissions on runtime
            requestPermissions(Constants.permissions, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initiateLocationManager();
                    loadData();
                    initLayout();
                }
        }
    }

    private void hideFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(fragment);
            ft.commit();
        }
    }

    private void showFragment(Fragment fragment) {
        // set the current fragment to this one
        currentFragment = fragment;
        if (!fragment.isAdded()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.mainFrame, fragment);
            ft.commit();
        } else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.show(fragment);
            ft.commit();
        }
    }

    private void removeFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(currentFragment);
        ft.commit();
    }

    public void selectTab(int fragmentNumber) {
        if (fragmentNumber == FRAGMENT_MAP) {
            hideFragment(currentFragment);
            showFragment(mapFragment);
            currentFragment = mapFragment;
        } else if (fragmentNumber == FRAGMENT_HISTORY) {
            hideFragment(currentFragment);
            showFragment(historyFragment);
            currentFragment = historyFragment;
        }
        TabLayout.Tab tab = tabLayoutDashboard.getTabAt(fragmentNumber);
        tab.select();
    }

    @Override
    public void mapReady() {
        mapFragment.moveCameraZoom(Constants.LAST_LOCATION);
        findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        findViewById(R.id.mainFrame).setVisibility(View.VISIBLE);
    }

    @Override
    public void mapButtonPressed(int buttonIdentifier) {
        switch (buttonIdentifier) {
            case Constants.FRAGMENT_BUTTON_START:
                tabLayoutDashboard.setVisibility(View.GONE);
                JOURNEY_STARTED = true;
                break;

            case Constants.FRAGMENT_BUTTON_PAUSE:
                JOURNEY_STARTED = false;
                tabLayoutDashboard.setVisibility(View.VISIBLE);

                break;

            case Constants.FRAGMENT_BUTTON_RESUME:
                JOURNEY_STARTED = true;
                break;

            case Constants.FRAGMENT_BUTTON_STOP:
                JOURNEY_STARTED = false;
                stopJourney();
                break;
        }
    }

    private void stopJourney() {

        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Do you want to save this run?")
                .setCancelText("No,Don't!")
                .setConfirmText("Yes,Save it!")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        removePolyLines();
                        sDialog.setTitleText("Cancelled!")
                                .setConfirmText("OK")
                                .showCancelButton(false)
                                .setCancelClickListener(null)
                                .setConfirmClickListener(null)
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {

                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        final Journey journey = saveJourney();
                        removePolyLines();
                        final Intent intent = new Intent(getApplicationContext(), Statistics.class);
                        intent.putExtra("journeyId", journey.getId());

                        sweetAlertDialog.setTitleText("Saved")
                                .setContentText("Your journey has been saved")
                                .setConfirmText("OK")
                                .showCancelButton(false)
                                .setCancelClickListener(null)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        System.out.println("Clicked");
                                        startActivityForResult(intent, 0);
                                        sweetAlertDialog.cancel();
                                        return;
                                    }
                                })
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                })
                .show();
        /*new AlertDialog.Builder(this)
                .setTitle("Caution")
                .setMessage("Do you want to save your journey ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveJourney();
                        removePolyLines();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removePolyLines();
                    }
                })
                .setIcon(android.R.drawable.ic_menu_help)
                .show();*/
    }


    private Journey saveJourney() {

        Journey journey = new Journey();
        journey.score = journeyScore;
        journey.date = Utils.getTodaysDate();
        journey.distance = journeyDistance;
        journey.hours = journeyHours;
        journey.mins = journeyMins;
        journey.seconds = journeySecs;
        journey.timestamp = Utils.getCurrentTimeStamp();
        journey.save();

        for (JourneyPath journeyPath : journeyPathList) {
            journeyPath.journey = journey;
            journeyPath.save();
        }

        for (JourneyTree journeyTree : journeyTreeList) {
            journeyTree.journey = journey;
            journeyTree.save();
        }

        journeyPathList.clear();
        journeyTreeList.clear();
        journeyDistance = 0;
        journeyHours = 0;
        journeyMins = 0;
        journeySecs = 0;
        journeyScore = 0;
        mapFragment.updateViews(0);

        return journey;
    }

    @Override
    public void onBackPressed() {   // this is fired if user presses the back button. its a good idea to ask the user before quitting the app
        finish();
    }

    protected void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }


    private void changeTabsFont(TabLayout tabLayout) {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Lato-Regular.ttf"));
                }
            }
        }
    }
}