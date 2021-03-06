package monash.sprintree.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import at.markushi.ui.CircleButton;
import monash.sprintree.R;
import monash.sprintree.activities.MapsActivity;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Marker;
import monash.sprintree.data.SpinnerItem;
import monash.sprintree.data.Tree;
import monash.sprintree.listAdapters.HistoryListAdapter;
import monash.sprintree.listAdapters.SpinnerAdapter;
import monash.sprintree.service.WikimediaService;
import monash.sprintree.service.WikimediaServiceComplete;
import monash.sprintree.service.TreeService;
import monash.sprintree.utils.CountDownAnimation;
import monash.sprintree.utils.MapWrapperLayout;
import monash.sprintree.utils.MultiDrawable;
import monash.sprintree.utils.Utils;

public class GMapFragment extends Fragment implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Marker>,
        ClusterManager.OnClusterInfoWindowClickListener<Marker>,
        ClusterManager.OnClusterItemClickListener<Marker>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Marker> {

    /* References
    1 - Clustering:
    Google map-utils (github)
     */

    /*
      Holds google map, timer, treee counter, selector icons for selecting various trees (Visited, All, Unique) and journey control buttons
      Start, Pause, Resume and stop
     */
    private ClusterManager<Marker> mClusterManager; // google map's api
    /*
    View Objects
     */
    View v;
    private GoogleMap mMap;
    private FragmentListener listener;
    MapWrapperLayout mapWrapperLayout;  // holds map inside

    CircleButton visitedButton, allButton, uncommonButton;  // 3 filter buttons

    LinearLayout clockView;

    TickerView treeScore;
    private Button startButton, pauseButton, resumeButton, stopButton;
    private TickerView timerValueHours;
    private TickerView timerValueMins;
    private TickerView timerValueSecs;
    private TextView countdown;
    CountDownAnimation countDownAnimation;

    private long startTime = 0L;

    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    int secs, mins, hrs;

    /*
    Data Objects
     */
    List<Marker> nonUniqueTrees;
    List<Marker> uniqueTrees;
    List<Marker> unlockedTrees;
    int displayAll = 0;

    public GMapFragment() {
    }

    public static GMapFragment newInstance(FragmentListener listener, List<Marker> nonUniqueTrees, List<Marker> uniqueTrees, List<Marker> unlockedTrees) {

        GMapFragment fragment = new GMapFragment();
        fragment.listener = listener;
        fragment.nonUniqueTrees = nonUniqueTrees;
        fragment.uniqueTrees = uniqueTrees;
        fragment.unlockedTrees = unlockedTrees;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_map, container, false);
        final com.google.android.gms.maps.MapFragment mapFragment = (com.google.android.gms.maps.MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapWrapperLayout = (MapWrapperLayout) v.findViewById(R.id.map_relative_layout);
        mapFragment.getMapAsync(this);
        initiateLayout(v);
        return v;
    }

    private void initiateLayout(View view) {

        clockView = (LinearLayout) view.findViewById(R.id.clockView);
        clockView.setVisibility(View.GONE);

        visitedButton = (CircleButton) view.findViewById(R.id.visitedButton);
        visitedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAll = 2;
                addMarkers(displayAll);
            }
        });
        allButton = (CircleButton) view.findViewById(R.id.allButton);
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAll = 0;
                addMarkers(displayAll);
            }
        });
        uncommonButton = (CircleButton) view.findViewById(R.id.uncommonButton);
        uncommonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAll = 1;
                addMarkers(displayAll);
            }
        });
        animateButtons();

        treeScore = (TickerView) view.findViewById(R.id.treeCounter);
        treeScore.setCharacterList(TickerUtils.getDefaultNumberList());
        treeScore.setText("0");

        timerValueHours = (TickerView) view.findViewById(R.id.timerHours);
        timerValueHours.setCharacterList(TickerUtils.getDefaultNumberList());
        timerValueHours.setText("00");

        timerValueMins = (TickerView) view.findViewById(R.id.timerMins);
        timerValueMins.setCharacterList(TickerUtils.getDefaultNumberList());
        timerValueMins.setText("00");

        timerValueSecs = (TickerView) view.findViewById(R.id.timerSecs);
        timerValueSecs.setCharacterList(TickerUtils.getDefaultNumberList());
        timerValueSecs.setText("00");

        countdown = (TextView) view.findViewById(R.id.countdown);
        startButton = (Button) view.findViewById(R.id.startButton);
        pauseButton = (Button) view.findViewById(R.id.pauseButton);
        resumeButton = (Button) view.findViewById(R.id.resumeButton);
        stopButton = (Button) view.findViewById(R.id.stopButton);

        //List<String> list = new ArrayList<String>();
        ArrayList<SpinnerItem> list = new ArrayList<>();
        list.add(new SpinnerItem("All trees", R.drawable.tree));
        list.add(new SpinnerItem("Uncommon", R.drawable.tree_unique));
        list.add(new SpinnerItem("Unlocked", R.drawable.tree_visited));

        /*treeView = (Spinner) view.findViewById(R.id.treeViewSpinner);

        spinnerAdapter = new SpinnerAdapter(getActivity(), R.layout.spinner_layout, R.id.txt, list);
        treeView.setAdapter(spinnerAdapter);
        treeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                displayAll = position;
                addMarkers(displayAll);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (startButton.getText().equals("START EXPLORING")) {
                    //((MapsActivity)getActivity()).hideTab();
                    startButton.setText("CANCEL");
                    countdownTimer(countdown, 3);
                    // initiate countdown timer, when its done, start the journey procedure
                } else {
                    startButton.setText("START EXPLORING");
                    countdown.cancelLongPress();
                    countDownAnimation.cancel();
                }
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                resumeButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                ((MapsActivity) getActivity()).hideTab();
                listener.mapButtonPressed(Constants.FRAGMENT_BUTTON_RESUME);
            }
        });
        //check

        pauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
                ((MapsActivity) getActivity()).showTab();
                listener.mapButtonPressed(Constants.FRAGMENT_BUTTON_PAUSE);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                customHandler.removeCallbacksAndMessages(null);
                timeInMilliseconds = 0L;
                startTime = 0L;
                timeSwapBuff = 0L;
                updatedTime = 0L;
                secs = 0;
                mins = 0;
                hrs = 0;
                timerValueHours.setText("00");
                timerValueMins.setText("00");
                timerValueSecs.setText("00");
                startButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
                ((MapsActivity) getActivity()).showTab();
                listener.mapButtonPressed(Constants.FRAGMENT_BUTTON_STOP);

            }
        });
    }

    public void updateViews(int score) {
        treeScore.setText(String.valueOf(score));
    }

    public void animateButtons() {
        Animation blinkAnimation = new AlphaAnimation(1, 0);
        blinkAnimation.setDuration(300);
        blinkAnimation.setInterpolator(new LinearInterpolator());
        blinkAnimation.setRepeatCount(4);
        blinkAnimation.setRepeatMode(Animation.REVERSE);

        if(visitedButton != null )
            visitedButton.startAnimation(blinkAnimation);
        if(allButton != null )
            allButton.startAnimation(blinkAnimation);
        if(uncommonButton != null )
            uncommonButton.startAnimation(blinkAnimation);
    }
    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            updatedTime = timeSwapBuff + timeInMilliseconds;

            secs = (int) (updatedTime / 1000);
            mins = secs / 60;
            hrs = mins / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerValueHours.setText(String.format("%02d", hrs));
            timerValueMins.setText(String.format("%02d", mins));
            timerValueSecs.setText(String.format("%02d", secs));

            listener.updateTimer(hrs, mins, secs);
            customHandler.postDelayed(this, 0);
        }

    };

    public void makeClockVisible(boolean visible) {
        clockView.setVisibility( visible ? View.VISIBLE : View.GONE );
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Destroy called ---------------");
        mMap.clear();
    }

    public Bitmap resizeTreeIcons(int width, int height, Marker marker) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), marker.image);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public Polyline addPolyLine(LatLng from, LatLng to) {
        return mMap.addPolyline(new PolylineOptions()
                .add(from, to)
                .width(4)
                .color(Color.RED));
    }

    // adds polylines to google map
    public Polyline addPolylines( PolylineOptions polylineOptions ) {
        return mMap.addPolyline(polylineOptions);
    }

    // displayAll acts as an identifier for display, 0 is all, 1 is unique and 2 is visited
    private void addMarkers(int displayAll) {

        if (isAdded()) {
            mMap.clear();

            mClusterManager = new ClusterManager<>(getActivity(), mMap);
            mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
            mMap.setOnCameraIdleListener(mClusterManager);
            mClusterManager.setRenderer(new OwnIconRendered(getActivity()));
            mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());
            mClusterManager.setOnClusterItemClickListener(GMapFragment.this);
            mClusterManager.setOnClusterClickListener(GMapFragment.this);
            mMap.setOnMarkerClickListener(mClusterManager);


            mClusterManager.clearItems();
            /* end custom info window clusters */
            if (displayAll == 0) {
                mClusterManager.addItems(nonUniqueTrees);
                mClusterManager.addItems(uniqueTrees);
                mClusterManager.addItems(unlockedTrees);

            } else if (displayAll == 1) {
                mClusterManager.addItems(uniqueTrees);
            } else if (displayAll == 2) {
                mClusterManager.addItems(unlockedTrees);
            }
            mClusterManager.cluster();
        }
    }

    // refresh marker lists
    public void reloadTrees(List<Marker> nonUniqueTrees, List<Marker> uniqueTrees, List<Marker> unlockedTrees) {
        this.uniqueTrees = uniqueTrees;
        this.nonUniqueTrees = nonUniqueTrees;
        this.unlockedTrees = unlockedTrees;
        addMarkers(displayAll);  // 0 = display all
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // add google maps style
        mMap = googleMap;
        /* for custom info window clusters */
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
       boolean success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                       getActivity(), R.raw.style_json));

        if (!success) {
            Log.e("MapsActivityRaw", "Style parsing failed.");
        }

        addMarkers(0);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            UiSettings uiSettings = mMap.getUiSettings();
            uiSettings.setCompassEnabled(false);
            uiSettings.setZoomControlsEnabled(true);
        } else {
            Toast.makeText(getActivity(), "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                UiSettings uiSettings = mMap.getUiSettings();
                uiSettings.setCompassEnabled(false);
                uiSettings.setZoomControlsEnabled(true);
            }
        }
        listener.mapReady();
    }

    // move camera
    public void moveCamera(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (mMap == null) {
                return;
            } else {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                mMap.animateCamera(cameraUpdate);
                Constants.LAST_LOCATION = location;
            }
        }
    }

    // move camera with zoom
    public void moveCameraZoom(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (mMap == null) {
                return;
            } else {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Constants.MAP_ZOOM);
                mMap.animateCamera(cameraUpdate);
            }
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Marker> cluster) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), 19.0f);
        mMap.animateCamera(cameraUpdate);
        return true;
        //Toast.makeText(getActivity(), "Zoom in to view trees", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Marker> cluster) {
        System.out.println("on cluster window click");
    }

    @Override
    public boolean onClusterItemClick(Marker marker) {
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Marker marker) {
    }

    private class OwnIconRendered extends DefaultClusterRenderer<Marker> {

        private final IconGenerator mClusterIconGenerator;
        Context context;

        OwnIconRendered(Context context) {
            super(context, mMap, mClusterManager);
            this.context = context.getApplicationContext();
            mClusterIconGenerator = new IconGenerator(context);
        }

        // create a custom cluster marker icon
        @Override
        protected void onBeforeClusterItemRendered(Marker item, MarkerOptions markerOptions) {
            if (GMapFragment.this.isAdded()) {
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeTreeIcons(24, 24, item)));
                markerOptions.snippet(item.getSnippet());
                markerOptions.title(item.getTitle());
                super.onBeforeClusterItemRendered(item, markerOptions);
            }
        }

        @Override
        public void setOnClusterClickListener(ClusterManager.OnClusterClickListener<Marker> listener) {
            super.setOnClusterClickListener(listener);
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Marker> cluster, MarkerOptions markerOptions) {
            final Drawable clusterIcon;
            if (displayAll == 0) {
                clusterIcon = getResources().getDrawable(R.drawable.tree);
            } else if (displayAll == 1) {
                clusterIcon = getResources().getDrawable(R.drawable.tree_unique);
            } else {
                clusterIcon = getResources().getDrawable(R.drawable.tree_visited);
            }

            //clusterIcon.setColorFilter(getResources().getColor(android.R.color.holo_orange_light), PorterDuff.Mode.SRC_ATOP);

            mClusterIconGenerator.setBackground(clusterIcon);
            mClusterIconGenerator.setTextAppearance(R.style.iconGenText);

            //modify padding for one or two digit numbers
            /*
            if (cluster.getSize() < 10) {
                mClusterIconGenerator.setContentPadding(40, 20, 0, 0);
            }
            else {
                mClusterIconGenerator.setContentPadding(30, 20, 0, 0);
            }*/

            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    // class to deal with custom infowindow, images from wikipedia can also be fetched
    private class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter, WikimediaServiceComplete {

        private View myContentsView;
        com.google.android.gms.maps.model.Marker lastMarker;
        private Tree lastTree;

        private MyCustomAdapterForItems() {
            myContentsView = getActivity().getLayoutInflater().inflate(
                    R.layout.marker_info_window, null);
        }

        @Override
        public View getInfoWindow(com.google.android.gms.maps.model.Marker marker) {

            lastMarker = marker;

            TextView tvTitle = ((TextView) myContentsView
                    .findViewById(R.id.txtTitle));
            TextView tvSnippet = ((TextView) myContentsView
                    .findViewById(R.id.txtSnippet));
            TextView details = ((TextView) myContentsView
                    .findViewById(R.id.details));
            TextView life = ((TextView) myContentsView
                    .findViewById(R.id.life));

            Tree tree = TreeService.findTreeByPosition(marker.getPosition());
            if (listener.isTreeVisited(tree)) {
                myContentsView.findViewById(R.id.layoutLock).setVisibility(View.GONE);
                myContentsView.findViewById(R.id.layoutInfo).setVisibility(View.VISIBLE);
                if (tree.commonName.equals("tba")) {
                    tvTitle.setText(tree.scientificName);
                } else {
                    tvTitle.setText(tree.commonName);
                }
                tvSnippet.setText("Genus: " + tree.genus);
                if ((tree.yearPlanted == 0)) {
                    details.setText("Year Planted: 1900");
                } else {
                    details.setText("Year Planted: " + String.valueOf(tree.yearPlanted));
                }
                if ((tree.usefulLifeExpectencyValue == 0)) {
                    life.setText("Life Expectancy: 10 yrs");
                    //life.setVisibility(View.GONE);
                } else {
                    //life.setVisibility(View.VISIBLE);
                    life.setText("Life Expectancy: " + String.valueOf(tree.usefulLifeExpectencyValue) + " yrs");
                }
            } else {
                myContentsView.findViewById(R.id.layoutInfo).setVisibility(View.GONE);
                myContentsView.findViewById(R.id.layoutLock).setVisibility(View.VISIBLE);
            }

            /*if( lastTree == null ) {
                if (!hasImage(imageView)) {
                    WikimediaService task = new WikimediaService(this, imageView, tree);
                    task.execute();
                }
            } else {
                 if (!lastTree.comId.equals(tree.comId)){
                     imageView.setImageDrawable(null);
                     WikimediaService task = new WikimediaService(this, imageView, tree);
                     task.execute();
                 }
            }*/
            lastTree = tree;
            return myContentsView;
        }

        @Override
        public View getInfoContents(final com.google.android.gms.maps.model.Marker marker) {
            return null;
        }

        @Override
        public void wikiImageComplete(JSONObject result, ImageView view) {
            String source = "";
            try {
                source = recurseKeys(result, "source");
                if (!source.equals("")) {
                    Picasso.with(getActivity()).load(source).into(view, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            lastMarker.setVisible(true);
                            lastMarker.hideInfoWindow();
                            getInfoWindow(lastMarker);
                            lastMarker.showInfoWindow();
                        }

                        @Override
                        public void onError() {

                        }
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

        String recurseKeys(JSONObject jObj, String findKey) throws JSONException {

            Iterator<?> keys = jObj.keys();
            String key = "";

            while (keys.hasNext() && !key.equalsIgnoreCase(findKey)) {
                key = (String) keys.next();

                if (key.equalsIgnoreCase(findKey)) {
                    return jObj.getString(key);
                }
                if (jObj.get(key) instanceof JSONObject) {
                    return recurseKeys((JSONObject) jObj.get(key), findKey);
                }
            }

            return "";
        }

        private boolean hasImage(@NonNull ImageView view) {
            Drawable drawable = view.getDrawable();
            boolean hasImage = (drawable != null);

            if (hasImage && (drawable instanceof BitmapDrawable)) {
                hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
            }
            return hasImage;
        }
    }

    public void countdownTimer(TextView textView, int timerValue) {
        countDownAnimation = new CountDownAnimation(textView, timerValue);
        Animation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        countDownAnimation.setAnimation(scaleAnimation);
        countDownAnimation.setCountDownListener(new CountDownAnimation.CountDownListener() {
            @Override
            public void onCountDownEnd(CountDownAnimation animation) {
                startButton.setText("START EXPLORING");
                Toast.makeText(getActivity(), "Your journey has started", Toast.LENGTH_SHORT).show();
                makeClockVisible(true);
                displayAll = 0;
                //treeView.setSelection(0, true);
                moveCamera(Constants.LAST_LOCATION);
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                startButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                listener.mapButtonPressed(Constants.FRAGMENT_BUTTON_START);
            }
        });
        countDownAnimation.start();
    }
}
