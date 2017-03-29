package monash.sprintree.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import monash.sprintree.R;
import monash.sprintree.activities.MapsActivity;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Tree;
import monash.sprintree.utils.MapWrapperLayout;
import monash.sprintree.utils.OnInfoWindowElemTouchListener;

public class GMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapsActivity listener;

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoButton;
    private OnInfoWindowElemTouchListener infoButtonListener;
    MapWrapperLayout mapWrapperLayout;

    public GMapFragment() {
        // Required empty public constructor
    }

    public static GMapFragment newInstance(MapsActivity listener ) {
        GMapFragment fragment = new GMapFragment();
        fragment.listener = listener;
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
        View v = inflater.inflate(R.layout.fragment_map, container, false);
        final com.google.android.gms.maps.MapFragment mapFragment = (com.google.android.gms.maps.MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map);
        mapWrapperLayout = (MapWrapperLayout) v.findViewById(R.id.map_relative_layout);
        mapFragment.getMapAsync(this);

        /*mapWrapperLayout.init(mMap, getPixelsFromDp(getActivity(), (39 + 20)));

        this.infoWindow = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.info_window, null);
        this.infoTitle = (TextView) infoWindow.findViewById(R.id.title);
        this.infoSnippet = (TextView) infoWindow.findViewById(R.id.snippet);
        this.infoButton = (Button) infoWindow.findViewById(R.id.button);
        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                //marker button pressed, tell main activity to start travelling here
                System.out.println("Marker pressed");
            }
        };*/

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void addMarkers() {
        for ( Tree t: Tree.listAll(Tree.class) ) {
            CircleOptions circleOptions = new CircleOptions()
                    .center(new LatLng(t.latitude, t.longitude))
                    .strokeColor(Color.GREEN)
                    .fillColor(Color.LTGRAY)
                    .radius(5); // In meters

            System.out.println(t.latitude +"-"+t.longitude);
// Get back the mutable Circle
            Circle circle = mMap.addCircle(circleOptions);
        }

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addMarkers();
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getActivity(), "You have to accept to enjoy all app's services!", Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void addMarker(Tree tree, LatLng position) {
        MarkerOptions options = new MarkerOptions();
        options.position(position);
        options.title(tree.commonName != null ? tree.commonName : "testTree" );
        options.snippet(String.valueOf(tree.comId));
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.common_full_open_on_phone));
        // Add marker to map !
        mMap.addMarker(options);
    }

    public void moveCamera(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, Constants.MAP_ZOOM);
        mMap.animateCamera(cameraUpdate);
        Constants.LAST_LOCATION = location;
    }
}
