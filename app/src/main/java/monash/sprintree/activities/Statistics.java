package monash.sprintree.activities;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import monash.sprintree.R;
import monash.sprintree.data.Journey;
import monash.sprintree.data.JourneyPath;
import monash.sprintree.data.JourneyTree;
import monash.sprintree.data.Tree;
import monash.sprintree.utils.Utils;

import static android.R.attr.cacheColorHint;
import static android.R.attr.path;

public class Statistics extends AppCompatActivity implements OnMapReadyCallback {

    private final int REQUEST_CODE_IMAGE_PICK = 1;

    ImageView journeyImageView;
    private TextView toolbarTitle;
    private List<TreePieEntry> pieTrees;
    private TextView scoreLabel, durationLabel, distanceLabel;
    private LinearLayout parentView;

    private Journey journey;
    private List<JourneyTree> journeyTrees;

    PieChart mChart;
    ArrayList<PieEntry> entries ;
    PieDataSet pieDataSet ;
    PieData pieData;
    Button saveButton;
    GoogleMap mMap;
    ArrayList<LatLng> coordList = new ArrayList<LatLng>();
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        if(coordList.size()>0) {
            PolylineOptions polylineOptions = new PolylineOptions();
            // Create polyline options with existing LatLng ArrayList
            polylineOptions.addAll(coordList);
            polylineOptions
                    .width(5)
                    .color(Color.RED);

            mMap.addPolyline(polylineOptions);

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordList.get(0), 17.0f);
            mMap.animateCamera(cameraUpdate);
        }


    }

    private class TreePieEntry{
        public TreePieEntry(String genus, int count) {
            this.name = genus;
            this.count = count;
        }
        String name;
        int count;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.fullScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            journey = Journey.findById(Journey.class, extras.getLong("journeyId"));
            journeyTrees = journey.getTrees();
            for ( JourneyPath journeyPath: journey.getPath()) {
                coordList.add(new LatLng(journeyPath.latitude, journeyPath.longitude));
            }
        }
        //fakeData();
        initiateToolbar();
        initiateLayout();

        final NestedScrollView scroll = (NestedScrollView) findViewById(R.id.scroll);
        ImageView transparent = (ImageView)findViewById(R.id.imagetrans);

        transparent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        scroll.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        scroll.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        scroll.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
    }

    private void fakeData() {

        journey = new Journey();
        journey.score = 200;
        journey.distance = 400;
        journey.timestamp = (long)12345322;
        journey.hours = 40000;

        journeyTrees = new ArrayList<>();

        JourneyTree journeyTree = new JourneyTree();
        Tree t = new Tree();
        t.genus = "Eucalyptus";
        journeyTree.tree = t;
        journeyTrees.add(journeyTree);

        Tree t1 = new Tree();
        t1.genus = "Ulmus";
        JourneyTree journeyTree1 = new JourneyTree();
        journeyTree1.tree = t1;
        journeyTrees.add(journeyTree1);

        Tree t2 = new Tree();
        t2.genus = "Alpha";
        JourneyTree journeyTree2 = new JourneyTree();
        journeyTree2.tree = t2;
        journeyTrees.add(journeyTree2);

        Tree t3 = new Tree();
        t3.genus = "Bravo";
        JourneyTree journeyTree3 = new JourneyTree();
        journeyTree3.tree = t3;
        journeyTrees.add(journeyTree3);

        JourneyTree journeyTree4 = new JourneyTree();
        Tree t4 = new Tree();
        t4.genus = "Eucalyptus";
        journeyTree4.tree = t4;
        journeyTrees.add(journeyTree4);
    }

    private SpannableString generateCenterText() {
        SpannableString s = new SpannableString( journeyTrees.size() >0 ? journeyTrees.size() + "\nTrees" : "No Trees Crossed\n:(");
        s.setSpan(new RelativeSizeSpan(2f), 0, s.length(), 0);
        return s;
    }

    private void initiateLayout() {

        scoreLabel = (TextView) findViewById(R.id.scoreLabel);
        durationLabel = (TextView) findViewById(R.id.durationLabel);
        distanceLabel = (TextView) findViewById(R.id.distanceLabel);
        journeyImageView = (ImageView) findViewById(R.id.journeyImage);
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SweetAlertDialog(Statistics.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Journey saved")
                        .setConfirmText("Ok").show();
            }
        });

        loadImageFromStorage(journey.journeyImagePath == null ? "" : journey.journeyImagePath);
        mChart = (PieChart) findViewById(R.id.chart);
        mChart.getDescription().setEnabled(false);


        mChart.setCenterText(generateCenterText());
        mChart.setCenterTextSize(10f);

        // radius of the center hole in percent of maximum radius
        mChart.setHoleRadius(50f);
        mChart.setTransparentCircleRadius(50f);


        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setDrawInside(false);
        l.setEnabled(false);

        // entry label styling
        mChart.setEntryLabelColor(Color.BLACK);

        entries = new ArrayList<>();
        AddValuesToPIEENTRY();
        pieDataSet = new PieDataSet(entries, "Trees");
        pieData = new PieData( pieDataSet);
        pieDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        pieDataSet.setValueTextSize(10f);
        pieDataSet.setValueTextColor(Color.BLACK);
        mChart.setData(pieData);
        mChart.animateY(3000);
    }

    public int isTreeInPieSeries(Tree tree) {
        for ( int counter = 0; counter < pieTrees.size(); counter++ ) {
            TreePieEntry pieTree = pieTrees.get(counter);
            if(pieTree.name.equals(tree.genus)) {
                return counter;
            }
        }
        return -1;
    }
    public void AddValuesToPIEENTRY(){
        pieTrees = new ArrayList<>();
        for (JourneyTree journeyTree : journeyTrees ) {
            int location =isTreeInPieSeries(journeyTree.tree);
            if( location != -1 ) {  // tree is in series, increment its counter
                TreePieEntry entry =  pieTrees.get(location);
                entry.count++;
                pieTrees.set(location, entry);
            } else {
                pieTrees.add( new TreePieEntry( journeyTree.tree.genus, 1 ));
            }
        }

        for( TreePieEntry pieTree : pieTrees ) {
            entries.add(new PieEntry(pieTree.count, pieTree.name));
        }

        scoreLabel.setText(String.valueOf(journey.score));
        String hours = journey.hours < 10 ? "0" + String.valueOf(journey.hours) : String.valueOf(journey.hours);
        String mins = journey.mins < 10 ? "0" + String.valueOf(journey.mins) : String.valueOf(journey.mins);
        String seconds = journey.seconds < 10 ? "0" + String.valueOf(journey.seconds) : String.valueOf(journey.seconds);
        durationLabel.setText( hours + ":" + mins + ":" + seconds );
        distanceLabel.setText(String.valueOf((Math.round(journey.distance))));
        toolbarTitle.setText( "History " + Utils.getDateCurrentTimeZone(journey.timestamp));
    }

    private void deleteJourney() {

        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Do you want to delete this run?")
                .setCancelText("No,Don't!")
                .setConfirmText("Yes,Delete it!")
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        journey.delete();
                        sDialog.setTitleText("Deleted!")
                                .setContentText("Your journey has been deleted!")
                                .setConfirmText("OK")
                                .showCancelButton(false)
                                .setCancelClickListener(null)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        Statistics.this.finish();
                                    }
                                })
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                    }
                })
                .show();
    }
    private void initiateToolbar() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        findViewById(R.id.toolbarDelete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteJourney();
            }
        });
        findViewById(R.id.toolbarShare).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot();
            }
        });
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            parentView = (LinearLayout) findViewById(R.id.parentView);
            // create bitmap screen capture
            //View v1 = getWindow().getDecorView().getRootView();
            View v1 = parentView.getRootView();

            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void openScreenshot(File imageFile) {
        /*Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(imageFile);
        intent.setDataAndType(uri, "image/*");
        startActivity(intent);*/

        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        imageUris.add(Uri.fromFile(imageFile)); // Add your image URIs here

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        shareIntent.setType("image/*");
        startActivity(Intent.createChooser(shareIntent, "Share images to.."));


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    public void imageUpload(View view) {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , REQUEST_CODE_IMAGE_PICK);//one can be replaced with any action code
    }
    private void loadImageFromStorage(String path)
    {
        try {
            File f=new File(path);
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            ImageView img=(ImageView)findViewById(R.id.journeyImage);
            img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode){
                case REQUEST_CODE_IMAGE_PICK:
                    Uri selectedImage = data.getData();
                    journeyImageView.setImageURI(selectedImage);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                        String path = saveToInternalStorage(bitmap, System.currentTimeMillis()+"_j.jpg");
                        journey.journeyImagePath = path;
                        journey.save();
                        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                                .setTitleText("Picture saved")
                                .setConfirmText("Ok").show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }

        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage, String imageName){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, imageName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
