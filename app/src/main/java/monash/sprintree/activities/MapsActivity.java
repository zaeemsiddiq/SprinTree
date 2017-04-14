package monash.sprintree.activities;

import android.Manifest;
import android.animation.Animator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompatBase;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

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
import monash.sprintree.service.TreeService;
import monash.sprintree.utils.Utils;

public class MapsActivity extends FragmentActivity implements LocationListener, FragmentListener {

    /*
    Fragment objects
     */
    private GMapFragment mapFragment;
    private HistoryFragment historyFragment;
    public static int FRAGMENT_MAP = 0; // used to map tab positions
    public static int FRAGMENT_HISTORY = 1;

    /*
    View Objects
     */
    private TabLayout tabLayoutDashboard;
    static final int REQUEST_PERMISSION_CODE = 100;
    Fragment currentFragment;
    LocationManager locationManager;


    /*
    Data objects
     */
    List<Marker> nonUniqueMarkers;
    List<Marker> uniqueMarkers;

    /*
    Journey Objects
     */
    boolean JOURNEY_STARTED;
    Journey journey;
    int journeyScore;
    List<JourneyPath> journeyPathList;
    List<JourneyTree> journeyTreeList;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Utils.fullScreen(MapsActivity.this);
        setContentView(R.layout.activity_maps);
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            handlePermissions();
        }
        else {
            initiateLocationManager();
            loadData();
            initLayout();
        }

    }

    private void loadData() {
        JOURNEY_STARTED = false;
        journey = new Journey();
        journeyScore = 0;
        journeyPathList = new ArrayList<>();
        journeyTreeList = new ArrayList<>();

        uniqueMarkers = new ArrayList<>();
        nonUniqueMarkers = new ArrayList<>();
        for( Tree tree : Constants.trees ) {
            if(tree.commonName != null) {
                if( tree.commonName.equals("Ulmus") ||
                        tree.commonName.equals("UNKNOWN") ||
                        tree.commonName.equals("Eucalyptus") ||
                        tree.commonName.equals("Ulmus") ) {
                    uniqueMarkers.add( new Marker(new LatLng(tree.latitude, tree.longitude), tree.commonName, tree.scientificName, R.mipmap.unique_tree, tree.comId));
                }
                else {
                    nonUniqueMarkers.add( new Marker(new LatLng(tree.latitude, tree.longitude), tree.commonName, tree.scientificName, R.drawable.tree, tree.comId));
                }
            } else {
                System.out.println("");
            }
        }
    }


    private void initLayout() {
        initiateTabsLayout();
        mapFragment = GMapFragment.newInstance(MapsActivity.this, nonUniqueMarkers, uniqueMarkers);
        historyFragment = HistoryFragment.newInstance(this);
        selectTab(FRAGMENT_MAP);
    }

    private void initiateTabsLayout() { // adding the tabs dynamically
        tabLayoutDashboard = (TabLayout) findViewById(R.id.mainTabs);
        tabLayoutDashboard.addTab(tabLayoutDashboard.newTab().setText("Map")); //0
        tabLayoutDashboard.addTab(tabLayoutDashboard.newTab().setText("History")); //1
        tabLayoutDashboard.addTab(tabLayoutDashboard.newTab().setText("My Forest")); //2
        tabLayoutDashboard.setTabMode(TabLayout.MODE_FIXED);

        tabLayoutDashboard.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                System.out.println(tab.getPosition());
                if(tab.getPosition() == 0) {
                    if(mapFragment != null) {
                        // hide the current fragment first
                        hideFragment(currentFragment);
                        // add/show the fragment
                        showFragment(mapFragment);
                        // set the current fragment to this one
                        currentFragment = mapFragment;
                    }
                }
                if(tab.getPosition() == 1) {
                    Toast.makeText(MapsActivity.this, "This feature will be added in upcoming versions", Toast.LENGTH_SHORT).show();
                    /*
                    if(historyFragment != null) {
                        // hide the current fragment first
                        hideFragment(currentFragment);
                        // add/show the fragment
                        showFragment(historyFragment);
                        // set the current fragment to this one
                        currentFragment = historyFragment;
                    }*/
                }
                if(tab.getPosition() == 2) {
                    Toast.makeText(MapsActivity.this, "This feature will be added in upcoming versions", Toast.LENGTH_SHORT).show();
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
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                2000,
                1, this);
        // Define the criteria how to select the location provider -> use
        // default
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "Allow permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        List<Journey> journeyList = Journey.find(Journey.class, "1");   // create a journey first
        Journey journey;
        if(journeyList.size() > 0 ) {
            journey = journeyList.get(0);
        } else {
            journey = new Journey();
            journey.score = 100;
            journey.date = Utils.getTodaysDate();
            journey.save();
        }

        Constants.LAST_LOCATION = location;
        float lat = (float) (location.getLatitude());
        float lng = (float) (location.getLongitude());

        if(JOURNEY_STARTED) {
            addJourneyPathToList(lat, lng);
            calculateAndAddNearestTree(lat, lng);
            mapFragment.moveCamera(location);
        }
        System.out.println("Location Changed");

    }

    @Override
    public boolean isTreeVisited(Tree tree) {
        boolean treeVisited = false;
        for( JourneyTree journeyTree : journeyTreeList) {
            if(journeyTree.tree.comId.equals(tree.comId) ){
                treeVisited = true;
                break;
            }
        }
        return treeVisited;
    }

    private void calculateAndAddNearestTree(float lat, float lng) {
        if(mapFragment != null) {
            //float[] results = new float[1]; // initialising a 1d result array to pass into distanceBetween method (source: developers.google.com)
            for(Marker marker : nonUniqueMarkers) {
                float[] results = new float[1];
                Location.distanceBetween(lat,lng, marker.getPosition().latitude, marker.getPosition().longitude, results); // in case of 0 previous stop is the starting stop
                if(results[0] < 10.00 ) {
                    Tree nearestTree = TreeService.findTreeByPosition(marker.getPosition());
                    if(!isTreeVisited(nearestTree)) {  // tree not visited before, add tree now
                        JourneyTree journeyTree = new JourneyTree();
                        journeyTree.tree = nearestTree;
                        journeyTreeList.add( journeyTree );
                        System.out.println("tree found" + marker.getTitle());
                        Toast.makeText(this, "tree found" + marker.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
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
    public void onProviderDisabled(String provider) {}


    @Override
    public void onProviderEnabled(String provider) {}

    private void handlePermissions() {
        if (Build.VERSION.SDK_INT> Build.VERSION_CODES.LOLLIPOP_MR1) {  // current version is Marshmallow, which requires permissions on runtime
            requestPermissions(Constants.permissions, REQUEST_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        System.out.println("on permission");
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                if( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initiateLocationManager();
                    loadData();
                    initLayout();
                }
        }
    }

    private void hideFragment(Fragment fragment) {
        if(fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(fragment);
            ft.commit();
        }
    }
    private void showFragment(Fragment fragment) {
        if(!fragment.isAdded()) {
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
        if(fragmentNumber == FRAGMENT_MAP) {
            hideFragment(currentFragment);
            showFragment(mapFragment);
            currentFragment = mapFragment;
        } else  if(fragmentNumber == FRAGMENT_HISTORY) {
            hideFragment(currentFragment);
            showFragment(historyFragment);
            currentFragment = historyFragment;
        }
        TabLayout.Tab tab = tabLayoutDashboard.getTabAt(fragmentNumber);
        tab.select();
    }

    @Override
    public void mapReady() {
        mapFragment.moveCamera(Constants.LAST_LOCATION);
        findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        findViewById(R.id.mainFrame).setVisibility(View.VISIBLE);
    }

    @Override
    public void mapButtonPressed(int buttonIdentifier) {
        switch (buttonIdentifier) {
            case Constants.FRAGMENT_BUTTON_START:
                JOURNEY_STARTED = true;
                break;

            case Constants.FRAGMENT_BUTTON_PAUSE:
                JOURNEY_STARTED = false;
                break;

            case Constants.FRAGMENT_BUTTON_RESUME:
                JOURNEY_STARTED = true;
                break;

            case Constants.FRAGMENT_BUTTON_STOP:
                JOURNEY_STARTED = false;
                new AlertDialog.Builder(this)
                        .setTitle("Caution")
                        .setMessage("Do you want to save your journey ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                saveJourney();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(android.R.drawable.ic_menu_help)
                        .show();
                break;
        }
    }

    private void saveJourney() {
        journey.score = journeyScore;
        journey.date = Utils.getTodaysDate();
        journey.timestamp = Utils.getCurrentTimeStamp();
        journey.save();
    }



    @Override
    public void onBackPressed() {   // this is fired if user presses the back button. its a good idea to ask the user before quitting the app
        new AlertDialog.Builder(this)
                .setTitle("Caution")
                .setMessage("Do you want to exit the application ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeFragment();
                        stopLocationUpdates();
                        setResult(Constants.REQUEST_EXIT);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.button_onoff_indicator_on)
                .show();
    }

    protected void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }
}