package monash.sprintree.service;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import monash.sprintree.activities.MapsActivity;

/**
 * Created by Zaeem on 5/10/2016.
 */

public class SyncService {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();
    MapsActivity listener;

    public SyncService(MapsActivity listener) {
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
        this.listener = listener;
    }

    public void syncTrees( int offset, int limit ) {   // start syncing stops
        // Write a message to the database

    }
}
