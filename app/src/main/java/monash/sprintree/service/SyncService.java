package monash.sprintree.service;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import monash.sprintree.activities.SplashScreen;
import monash.sprintree.data.Constants;

/**
 * Created by Zaeem on 5/10/2016.
 */

public class SyncService {
    SplashScreen listener;
    FirebaseDatabase database;
    DatabaseReference myRef;


    public SyncService(SplashScreen listener) {
        this.listener = listener;
        database = FirebaseDatabase.getInstance();
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();
    }

    public void firebaseStart() {
        try {
            myRef.orderByKey().limitToFirst(Constants.FIREBASE_PAGE_SIZE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    String lastComId = TreeService.saveTrees(dataSnapshot);
                    listener.pageComplete(lastComId);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    //Log.w(TAG, "Failed to read value.", error.toException());
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void firebaseReload(String startComId) {
        try {
            myRef.orderByKey().startAt(startComId).limitToFirst(Constants.FIREBASE_PAGE_SIZE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String lastComId = TreeService.saveTrees(dataSnapshot);
                    listener.pageComplete(lastComId);
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
