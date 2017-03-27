package monash.sprintree.service;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import monash.sprintree.activities.MapsActivity;
import monash.sprintree.activities.Splash;

/**
 * Created by Zaeem on 5/10/2016.
 */

public class SyncService {
    Splash listener;
    FirebaseDatabase database;
    DatabaseReference myRef;

    public SyncService(Splash listener) {
        this.listener = listener;
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    private void firebase(){
        try {
            // Read from fire base
            myRef.orderByKey().limitToFirst(10).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    Object key = dataSnapshot.getKey();
                    Object value = dataSnapshot.getValue();
                    ArrayList parent = (ArrayList) value;
                    ArrayList child = (ArrayList) parent.get(0);
                    System.out.println(child.get(8));
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


    public void syncTrees( int offset, int limit ) {   // start syncing stops
        // Write a message to the database
        firebase();

    }
}
