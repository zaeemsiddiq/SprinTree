package monash.sprintree.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Marker;
import monash.sprintree.data.Tree;
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
    1 - Info Window Reload after loading the picture dynamically:
    http://stackoverflow.com/questions/16662484/why-custom-infowindow-of-google-map-v2-not-load-url-image

    2 - Clustering:
    Google map-utils (github)

    3 - Image downloading from URL:
    http://square.github.io/picasso/

    4 - Opentrees.org wikimedia data loading
    https://stevebennett.me/2015/04/07/opentrees-org-how-to-aggregate-373000-trees-from-9-open-data-sources/
    http://www.opentrees.org/v1/index.html#Melbourne-1287384

    5 -
     */
    private ClusterManager<Marker> mClusterManager;
    /*
    View Objects
     */
    View v;
    private GoogleMap mMap;
    private FragmentListener listener;

    MapWrapperLayout mapWrapperLayout;

    private Button startButton, pauseButton, resumeButton, stopButton;
    private TextView timerValue;
    private TextView countdown;
    CountDownAnimation countDownAnimation;
    Spinner treeView;

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
    boolean displayAll = true;
    public GMapFragment() { }

    public static GMapFragment newInstance( FragmentListener listener, List<Marker> nonUniqueTrees, List<Marker> uniqueTrees) {

        GMapFragment fragment = new GMapFragment();
        fragment.listener = listener;
        fragment.nonUniqueTrees = nonUniqueTrees;
        fragment.uniqueTrees = uniqueTrees;

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
        timerValue = (TextView) view.findViewById(R.id.timerValue);
        countdown = (TextView) view.findViewById(R.id.countdown);
        startButton = (Button) view.findViewById(R.id.startButton);
        pauseButton = (Button) view.findViewById(R.id.pauseButton);
        resumeButton = (Button) view.findViewById(R.id.resumeButton);
        stopButton = (Button) view.findViewById(R.id.stopButton);

        List<String> list = new ArrayList<String>();
        list.add("All trees");
        list.add("Unique");
        treeView = (Spinner) view.findViewById(R.id.treeViewSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        treeView.setAdapter(dataAdapter);
        treeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == 0) {
                    displayAll = true;
                } else {
                    displayAll = false;
                }
                addMarkers(displayAll);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (startButton.getText().equals("START")) {
                    startButton.setText("CANCEL");
                    countdownTimer(countdown, 3);   // initiate countdown timer, when its done, start the journey procedure
                } else {
                    startButton.setText("START");
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
                //timerValue.setVisibility(View.VISIBLE);
                listener.mapButtonPressed(Constants.FRAGMENT_BUTTON_RESUME);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);
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
                timerValue.setText("00:00:00");
                startButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);

                listener.mapButtonPressed(Constants.FRAGMENT_BUTTON_STOP);

            }
        });
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
            timerValue.setText(String.format("%02d", hrs) + ":" + String.format("%02d", mins) + ":"
                    + String.format("%02d", secs));
            customHandler.postDelayed(this, 0);
        }

    };

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

    public Bitmap resizeTreeIcons(int width, int height, Marker marker){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),marker.image);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void addMarkers(boolean displayAll ) {

        if(isAdded()) {
            mMap.clear();

            mClusterManager = new ClusterManager<>(getActivity(), mMap);
            mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
            mMap.setOnCameraIdleListener(mClusterManager);
            mClusterManager.setRenderer(new OwnIconRendered(getActivity()));
            mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());
            mClusterManager.setOnClusterItemClickListener(this);

            mClusterManager.clearItems();
            /* end custom info window clusters */
            if(displayAll) {
                mClusterManager.addItems(nonUniqueTrees);
                mClusterManager.addItems(uniqueTrees);

            } else {
                mClusterManager.addItems(uniqueTrees);
            }
            mClusterManager.cluster();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        /* for custom info window clusters */
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        addMarkers(true);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getActivity(), "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
        listener.mapReady();
    }

    public void moveCamera(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(mMap == null) {
            return;
        } else {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Constants.MAP_ZOOM);
            mMap.animateCamera(cameraUpdate);
            Constants.LAST_LOCATION = location;
        }
    }

    @Override
    public boolean onClusterClick(Cluster<Marker> cluster) {
        System.out.println("");
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Marker> cluster) {
        System.out.println("");
    }

    @Override
    public boolean onClusterItemClick(Marker marker) {
        System.out.println("");
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(Marker marker) {
    }

    /**
     * Draws profile photos inside markers (using IconGenerator).
     * When there are multiple people in the cluster, draw multiple photos (using MultiDrawable).
     */
    private class MarkerRenderer extends DefaultClusterRenderer<Marker> {
        private final IconGenerator mIconGenerator = new IconGenerator(getActivity());
        private final IconGenerator mClusterIconGenerator = new IconGenerator(getActivity());
        private final ImageView mImageView;
        private final ImageView mClusterImageView;
        private final int mDimension;

        public MarkerRenderer(Context context) {
            super(context, mMap, mClusterManager);

            View multiProfile = getActivity().getLayoutInflater().inflate(R.layout.marker_layout, null);
            mClusterIconGenerator.setContentView(multiProfile);
            mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

            mImageView = new ImageView(getActivity());
            mDimension = 20;
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
            int padding = 10;
            mImageView.setPadding(padding, padding, padding, padding);
            mIconGenerator.setContentView(mImageView);
        }

        @Override
        protected void onBeforeClusterItemRendered(Marker marker, MarkerOptions markerOptions) {
            // Draw a single person.
            // Set the info window to show their name.
            mImageView.setImageResource(marker.image);
            Bitmap icon = mIconGenerator.makeIcon();
            markerOptions
                    .icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .title(marker.getTitle());
        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Marker> cluster, MarkerOptions markerOptions) {
            // Draw multiple people.
            // Note: this method runs on the UI thread. Don't spend too much time in here (like in this example).
            List<Drawable> profilePhotos = new ArrayList<Drawable>(Math.min(4, cluster.getSize()));
            int width = mDimension;
            int height = mDimension;

            for (Marker p : cluster.getItems()) {
                // Draw 4 at most.
                if (profilePhotos.size() == 4) break;
                Drawable drawable = getResources().getDrawable(p.image);
                drawable.setBounds(0, 0, width, height);
                profilePhotos.add(drawable);
            }
            MultiDrawable multiDrawable = new MultiDrawable(profilePhotos);
            multiDrawable.setBounds(0, 0, width, height);

            mClusterImageView.setImageDrawable(multiDrawable);
            Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster cluster) {
            // Always render clusters.
            return cluster.getSize() > 1;
        }
    }

    private class OwnIconRendered extends DefaultClusterRenderer<Marker> {

        private final IconGenerator mClusterIconGenerator;
        Context context;

        public OwnIconRendered(Context context) {
            super(context, mMap, mClusterManager);
            this.context = context;
            mClusterIconGenerator = new IconGenerator(context);
        }

        @Override
        protected void onBeforeClusterItemRendered(Marker item, MarkerOptions markerOptions) {
            if( GMapFragment.this.isAdded() ) {
                markerOptions.icon( BitmapDescriptorFactory.fromBitmap( resizeTreeIcons(24,24, item) ) );
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
        protected void onBeforeClusterRendered(Cluster<Marker> cluster, MarkerOptions markerOptions){
            final Drawable clusterIcon;
            if(displayAll) {
                clusterIcon = getResources().getDrawable(R.drawable.tree);
            } else {
                clusterIcon = getResources().getDrawable(R.mipmap.unique_tree);
            }

            //clusterIcon.setColorFilter(getResources().getColor(android.R.color.holo_orange_light), PorterDuff.Mode.SRC_ATOP);

            mClusterIconGenerator.setBackground(clusterIcon);

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
            return null;
        }

        @Override
        public View getInfoContents(final com.google.android.gms.maps.model.Marker marker) {
            lastMarker = marker;

            TextView tvTitle = ((TextView) myContentsView
                    .findViewById(R.id.txtTitle));
            TextView tvSnippet = ((TextView) myContentsView
                    .findViewById(R.id.txtSnippet));
            ImageView imageView = ((ImageView) myContentsView
                    .findViewById(R.id.wikiImage));


            Tree tree = TreeService.findTreeByPosition(marker.getPosition());
            if(listener.isTreeVisited(tree)) {
                myContentsView.findViewById(R.id.layoutLock).setVisibility(View.GONE);
                myContentsView.findViewById(R.id.layoutInfo).setVisibility(View.VISIBLE);
                tvTitle.setText(marker.getTitle());
                tvSnippet.setText(marker.getSnippet());
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
        public void wikiImageComplete(JSONObject result, ImageView view) {
            String source = "";
            try {
                source = recurseKeys(result, "source");
                if(!source.equals("")) {
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
                    return recurseKeys((JSONObject)jObj.get(key), findKey);
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
                startButton.setText("START");
                Toast.makeText(getActivity(), "Your journey has started", Toast.LENGTH_SHORT).show();
                treeView.setSelection(0,true);
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
