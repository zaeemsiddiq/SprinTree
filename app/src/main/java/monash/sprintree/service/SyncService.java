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

public class SyncService {
    private Splash listener;
    private DatabaseReference myRef;

    public SyncService(Splash listener) {
        this.listener = listener;
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    private void firebaseStart( ){
        try {
            myRef.orderByKey().limitToFirst(Constants.FIREBASE_PAGE_SIZE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String lastAddedComId = TreeService.saveTree(dataSnapshot);
                    listener.pageComplete(lastAddedComId);
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
        firebaseStart();
    }
}
