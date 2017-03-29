package monash.sprintree.service;


import android.widget.NumberPicker;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import monash.sprintree.activities.MapsActivity;
import monash.sprintree.activities.Splash;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Tree;

/**
 * Created by Zaeem on 5/10/2016.
 */

/*public class SyncService {
    Splash listener;
    DatabaseReference myRef;

    public SyncService(Splash listener) {
        this.listener = listener;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    public void firebaseStart( ){
        try {
            myRef.orderByKey().limitToFirst(Constants.FIREBASE_PAGE_SIZE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    System.out.print("abc");
                    TreeService.saveTree(dataSnapshot);
                    listener.pageComplete(TreeService.saveTree(dataSnapshot));
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    //Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void firebaseReload( String startComId ) {
        try {
            //myRef.orderByKey().startAt("1013395");
            myRef.orderByKey().startAt(startComId).limitToFirst(Constants.FIREBASE_PAGE_SIZE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Object key = dataSnapshot.getKey();
                    Object value = dataSnapshot.getValue();

                    for( DataSnapshot tree: dataSnapshot.getChildren()) {
                        String comId = tree.getKey();
                        for( DataSnapshot columns: tree.getChildren() ) {
                            String attribute = columns.getKey();
                            Object data = (Object)columns.getValue();
                        }
                    }
                    listener.loadComplete();
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void syncTrees(  ) {   // start syncing stops
        // Write a message to the database
        System.out.print("test");
        firebaseStart();
    }
}*/


public class SyncService {
    Splash listener;
    FirebaseDatabase database;
    DatabaseReference myRef;


    public SyncService(Splash listener) {
        this.listener = listener;
        database = FirebaseDatabase.getInstance();
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    public void firebaseStart( ){
        try {
            myRef.orderByKey().limitToFirst(1000).addValueEventListener(new ValueEventListener() {
            //myRef.orderByKey().limitToFirst(Constants.FIREBASE_PAGE_SIZE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Object key = dataSnapshot.getKey();
                    Object value = dataSnapshot.getValue();

                    String comId = "";
                    for( DataSnapshot tree: dataSnapshot.getChildren()) {
                        comId = tree.getKey();
                        Tree t= new Tree();
                        t.comId=comId;
                    for( DataSnapshot columns: tree.getChildren() ) {
                        String attribute = columns.getKey();
                        Object data = (Object)columns.getValue();
                        if (attribute=="Age Description")
                            t.ageDescription=(String)data;
                        if(attribute=="Common Name")
                            t.commonName=(String)data;
                        if(attribute=="CoordinateLocation")
                            t.coordinateLocation=(String)data;
                        if(attribute=="Date Planted")
                            t.datePlanted=(String)data;
                        if(attribute=="Diameter Breast Height")
                            t.diameter=(Long)data;
                        if(attribute=="Easting")
                            t.easting=(Double)data;
                        if(attribute=="Family")
                            t.family=(String)data;
                        if(attribute=="Genus")
                            t.genus=(String)data;
                        if(attribute=="Latitude")
                            t.latitude=(Double)data;
                        if(attribute=="Located in")
                            t.locatedIn=(String)data;
                        if(attribute=="Longitude")
                            t.longitude=(Double)data;
                        if(attribute=="Northing")
                            t.northing=(Double)data;
                        if(attribute=="Precinct")
                            t.precinct=(String)data;
                        if(attribute=="Scientific Name")
                            t.scientificName=(String)data;
                        if(attribute=="UploadDate")
                            t.uploadDate=(String)data;
                        if(attribute=="Useful Life Expectancy")
                            t.usefulLifeExpectency=(String)data;
                        if(attribute=="Useful Life Expectancy Value")
                            t.usefulLifeExpectencyValue=(Long)data;
                        if(attribute=="Year Planted")
                            t.yearPlanted=(Long)data;
                            t.save();
                        int n= 0;
                        System.out.print("asds");
                    }
                }
                    listener.pageComplete(comId);


                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    //Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }


  public void firebaseReload( String startComId ) {
        try {
            //myRef.orderByKey().startAt("1013395");
            myRef.orderByKey().startAt(startComId).limitToFirst(Constants.FIREBASE_PAGE_SIZE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Object key = dataSnapshot.getKey();
                    Object value = dataSnapshot.getValue();

                    for( DataSnapshot tree: dataSnapshot.getChildren()) {
                        String comId = tree.getKey();
                        for( DataSnapshot columns: tree.getChildren() ) {
                            String attribute = columns.getKey();
                            Object data = (Object)columns.getValue();
                        }
                    }
                    listener.loadComplete();
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void syncTrees(  ) {   // start syncing stops
        // Write a message to the database
        System.out.print("test");
        firebaseStart();
    }
}
