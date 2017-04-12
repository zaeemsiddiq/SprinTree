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
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Marker;
import monash.sprintree.data.Tree;
import monash.sprintree.fragments.FragmentListener;
import monash.sprintree.fragments.GMapFragment;
import monash.sprintree.fragments.HistoryFragment;
import monash.sprintree.utils.Utils;

public class MapsActivity extends FragmentActivity implements LocationListener, FragmentListener {

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

        uniqueMarkers = new ArrayList<>();
        nonUniqueMarkers = new ArrayList<>();
        for( Tree tree : Constants.trees ) {
            if( tree.commonName.equals("Ulmus") ||
                    tree.commonName.equals("UNKNOWN") ||
                    tree.commonName.equals("Eucalyptus") ||
                    tree.commonName.equals("Ulmus") ) {
                uniqueMarkers.add( new Marker(new LatLng(tree.latitude, tree.longitude), tree.commonName, tree.commonName, R.mipmap.unique_tree, tree.comId));
            }
            else
            nonUniqueMarkers.add( new Marker(new LatLng(tree.latitude, tree.longitude), tree.commonName, tree.commonName, R.drawable.tree, tree.comId));

        }
    }


    private void initLayout() {
        initiateTabsLayout();
        Constants.mapFragment = GMapFragment.newInstance(MapsActivity.this, nonUniqueMarkers, uniqueMarkers);
        Constants.historyFragment = HistoryFragment.newInstance(this);
        selectTab(Constants.FRAGMENT_MAP);
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
                    if(Constants.mapFragment != null) {
                        // hide the current fragment first
                        hideFragment(currentFragment);
                        // add/show the fragment
                        showFragment(Constants.mapFragment);
                        // set the current fragment to this one
                        currentFragment = Constants.mapFragment;
                    }
                }
                if(tab.getPosition() == 1) {
                    Toast.makeText(MapsActivity.this, "This feature will be added in upcoming versions", Toast.LENGTH_SHORT).show();
                    /*
                    if(Constants.historyFragment != null) {
                        // hide the current fragment first
                        hideFragment(currentFragment);
                        // add/show the fragment
                        showFragment(Constants.historyFragment);
                        // set the current fragment to this one
                        currentFragment = Constants.historyFragment;
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
        Constants.LAST_LOCATION = location;
        float lat = (float) (location.getLatitude());
        float lng = (float) (location.getLongitude());

        System.out.println("Location Changed");
        if(Constants.mapFragment != null) {
            /*float[] results = new float[1]; // initialising a 1d result array to pass into distanceBetween method (source: developers.google.com)
            Location.distanceBetween(lat,lng, previousStop.getStopLatitude(), previousStop.getStopLongitude(),results); // in case of 0 previous stop is the starting stop
            System.out.println("*******StopsWalker" + results[0]);

            if(results[0] < 500 ) {

            }*/
            //Constants.mapFragment.moveCamera(location);
        }
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
        if(fragmentNumber == Constants.FRAGMENT_MAP) {
            hideFragment(currentFragment);
            showFragment(Constants.mapFragment);
            currentFragment = Constants.mapFragment;
        } else  if(fragmentNumber == Constants.FRAGMENT_HISTORY) {
            hideFragment(currentFragment);
            showFragment(Constants.historyFragment);
            currentFragment = Constants.historyFragment;
        }
        TabLayout.Tab tab = tabLayoutDashboard.getTabAt(fragmentNumber);
        tab.select();
    }

    @Override
    public void mapReady() {

        Constants.mapFragment.moveCamera(Constants.LAST_LOCATION);
        findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
        findViewById(R.id.mainFrame).setVisibility(View.VISIBLE);
    }

    @Override
    public void mapButtonPressed(int buttonIdentifier) {

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
        System.out.println("Stoping location updates");
        locationManager.removeUpdates(this);
    }
}
