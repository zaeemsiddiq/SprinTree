package monash.sprintree.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Marker;
import monash.sprintree.data.Tree;
import monash.sprintree.utils.MapWrapperLayout;
import monash.sprintree.utils.MultiDrawable;

public class GMapFragment extends Fragment implements OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<Marker>,
        ClusterManager.OnClusterInfoWindowClickListener<Marker>,
        ClusterManager.OnClusterItemClickListener<Marker>,
        ClusterManager.OnClusterItemInfoWindowClickListener<Marker> {

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
    List<Tree> greenTrees;
    List<Tree> uniqueTrees;
    List<Marker> markers;

    public GMapFragment() { }

    public static GMapFragment newInstance( FragmentListener listener, List<Tree> greenTrees, List<Tree> uniqueTrees, List<Marker>markers ) {
        GMapFragment fragment = new GMapFragment();
        fragment.listener = listener;
        fragment.greenTrees = greenTrees;
        fragment.uniqueTrees = uniqueTrees;
        fragment.markers = markers;
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
        startButton = (Button) view.findViewById(R.id.startButton);
        pauseButton = (Button) view.findViewById(R.id.pauseButton);
        resumeButton = (Button) view.findViewById(R.id.resumeButton);
        stopButton = (Button) view.findViewById(R.id.stopButton);

        List<String> list = new ArrayList<String>();
        list.add("All");
        list.add("Unique");
        treeView = (Spinner) view.findViewById(R.id.treeViewSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        treeView.setAdapter(dataAdapter);
        treeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "Selected"+position, Toast.LENGTH_SHORT).show();
                if(position == 0) {
                    addMarkers(false);
                } else {
                    addMarkers(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                startButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.VISIBLE);
            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                resumeButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.VISIBLE);
                //timerValue.setVisibility(View.VISIBLE);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                pauseButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.VISIBLE);

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                customHandler.removeCallbacksAndMessages(null);
                timeInMilliseconds = 0L ;
                startTime = 0L ;
                timeSwapBuff = 0L ;
                updatedTime = 0L ;
                secs = 0 ;
                mins = 0 ;
                hrs = 0 ;
                timerValue.setText("00:00:00");
                startButton.setVisibility(View.VISIBLE);
                stopButton.setVisibility(View.GONE);
                resumeButton.setVisibility(View.GONE);
                pauseButton.setVisibility(View.GONE);
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

    protected void RefreshMap() {

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

    public Bitmap resizeTreeIcons(int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tree);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    private void addMarkers(boolean uniqueTreesDisplay ) {
        if(isAdded()) {
            mMap.clear();
            Bitmap tree = resizeTreeIcons(24,24);
            if(uniqueTreesDisplay) {
                for ( Tree t: uniqueTrees ) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(t.latitude, t.longitude))
                            .title(t.commonName)
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.tree))
                            .icon(BitmapDescriptorFactory.fromBitmap( resizeTreeIcons(24,24) ))
                    );
                }
            } else {
                for ( Tree t: greenTrees ) {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(t.latitude, t.longitude))
                            .title(t.commonName)
                            //.icon(BitmapDescriptorFactory.fromResource(R.drawable.tree))
                            .icon(BitmapDescriptorFactory.fromBitmap(tree))
                    );
                }
            }
        }

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        /* for custom info window clusters */

        mClusterManager = new ClusterManager<>(getActivity(), mMap);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnCameraIdleListener(mClusterManager);
        mClusterManager.setRenderer(new OwnIconRendered(getActivity()));
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomAdapterForItems());
        /* end custom info window clusters */
        /* for simple clusters */
        //mClusterManager.setRenderer(new MarkerRenderer(getActivity()));
        //mMap.setOnMarkerClickListener(mClusterManager);
        //mMap.setOnInfoWindowClickListener(mClusterManager);
        //mClusterManager.setOnClusterClickListener(this);
        //mClusterManager.setOnClusterInfoWindowClickListener(this);
        //mClusterManager.setOnClusterItemClickListener(this);
        //mClusterManager.setOnClusterItemInfoWindowClickListener(this);
        /* end simple clusters */

        mClusterManager.addItems(markers);
        mClusterManager.cluster();





        //addMarkers(false);

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
        }
        else {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Constants.MAP_ZOOM);
            mMap.animateCamera(cameraUpdate);
            Constants.LAST_LOCATION = location;
        }

    }

    @Override
    public boolean onClusterClick(Cluster<Marker> cluster) {
        return false;
    }

    @Override
    public void onClusterInfoWindowClick(Cluster<Marker> cluster) {

    }

    @Override
    public boolean onClusterItemClick(Marker marker) {
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
            mImageView.setImageResource(R.drawable.tree);
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
                markerOptions.icon( BitmapDescriptorFactory.fromBitmap( resizeTreeIcons(24,24) ) );
                markerOptions.snippet(item.getSnippet());
                markerOptions.title(item.getTitle());
                super.onBeforeClusterItemRendered(item, markerOptions);
            }

        }

        @Override
        protected void onBeforeClusterRendered(Cluster<Marker> cluster, MarkerOptions markerOptions){

            final Drawable clusterIcon = getResources().getDrawable(R.drawable.tree);
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

    private class MyCustomAdapterForItems implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        public MyCustomAdapterForItems() {
            myContentsView = getActivity().getLayoutInflater().inflate(
                    R.layout.marker_info_window, null);
        }

        @Override
        public View getInfoWindow(com.google.android.gms.maps.model.Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(com.google.android.gms.maps.model.Marker marker) {
            TextView tvTitle = ((TextView) myContentsView
                    .findViewById(R.id.txtTitle));
            TextView tvSnippet = ((TextView) myContentsView
                    .findViewById(R.id.txtSnippet));

            tvTitle.setText(marker.getTitle());
            tvSnippet.setText(marker.getSnippet());

            return myContentsView;
        }
    }
}
