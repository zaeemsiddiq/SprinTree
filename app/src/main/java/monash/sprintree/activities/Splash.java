package monash.sprintree.activities;

import monash.sprintree.data.Tree;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.orm.SugarRecord;


import java.util.ArrayList;
import java.util.List;

import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Tree;
import monash.sprintree.service.SyncService;
import monash.sprintree.service.SyncServiceComplete;

public class Splash extends AppCompatActivity implements SyncServiceComplete {

    /*
    View objects
     */
    ProgressBar syncProgress;

    /*
    Data objects
     */
    private int loadedTrees;
    SyncService service;

    private void fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        fullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        initiateLayout();

        if (Tree.listAll(Tree.class).size() == 0) { // if the database is empty, load the trees from firebase
            service = new SyncService(this);
            service.firebaseStart();
        } else {
            startMapsActivity();
        }
        //Tree.deleteAll(Tree.class);
        //service = new SyncService(this);
        //service.firebaseStart();
        //List<Tree> treeList = Tree.listAll(Tree.class);
        //System.out.println("Size of the tree table is "+treeList.size());
        //System.out.println("");
    }

    private void startMapsActivity() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivity(mapIntent);
    }

    public void launchHistory(View view) {
        //Intent home = new Intent(this, Home.class);
        //startActivity(home);
    }

    private int getProgressPercentage(int n) {
        double numer = (double) n;
        double denom = (double) Constants.TOTAL_TREES;
        double d = ((numer / denom) * 100);
        return (int) d;
    }

    private void initiateLayout() {
        loadedTrees = 0;
        syncProgress = (ProgressBar) findViewById(R.id.mainProgressBar);
        syncProgress.setProgress(0);
    }

    @Override
    public void pageComplete(String comId) {
        loadedTrees += Constants.FIREBASE_PAGE_SIZE;
        if (loadedTrees >= Constants.TOTAL_TREES) {
            loadComplete();
        } else {
            syncProgress.setProgress(getProgressPercentage(loadedTrees));
            service.firebaseReload(comId);
        }

    }

    public void launchHome(View view) {
        startMapsActivity();
    }

    @Override
    public void loadComplete() {
        Toast.makeText(this, "Data Loading Complete", Toast.LENGTH_SHORT).show();
        startMapsActivity();
    }
}
