package monash.sprintree.activities;

import monash.sprintree.data.Journey;
import monash.sprintree.data.JourneyPath;
import monash.sprintree.data.Tree;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.service.SyncService;
import monash.sprintree.service.SyncServiceComplete;
import monash.sprintree.utils.Utils;

public class Splash extends AppCompatActivity implements SyncServiceComplete {

    /*
    View objects
     */
    ProgressBar syncProgress;

    /*
    Data objects
     */
    private int loadedTrees;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        Utils.fullScreen(Splash.this);
        setContentView(R.layout.activity_splash);
        if (getIntent().getBooleanExtra("Exit me", false)) {
            finish();
        }
        initiateLayout();
        //deleteDB();
        //Utils.exportDatabse( this );
        startLoading();
        //test();
    }
    private void test() {
        List<Journey> journeys = Journey.listAll(Journey.class);
        List<JourneyPath> journeyPaths = journeys.get(0).getPath();
        System.out.println(journeys.size());
    }

    private void startLoading() {
        new Thread() {
            @Override
            public void run() {
                final List<Tree> trees = Tree.findWithQuery(Tree.class, "SELECT * FROM TREE");
                final List<Tree> commonTrees = Tree.findWithQuery(Tree.class, "SELECT COUNT(genus), genus FROM TREE GROUP BY genus ORDER BY count(genus) DESC LIMIT 10");

                Constants.trees = trees;
                Constants.commonTrees = commonTrees;
                try {
                    // code runs in a thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (trees.size() == 0) { // if the database is empty, load the trees from firebase
                                try {
                                    Utils.openRenderer(getApplicationContext(), "structured2.json");
                                    startMapsActivity();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                startMapsActivity();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void deleteDB() {
        Utils.deleteDB(getApplicationContext());
    }

    private void startMapsActivity() {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        startActivityForResult(mapIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        finish();
    }

    public void launchHistory(View view) {
        //Intent home = new Intent(this, Statistics.class);
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
    public void pageComplete(final String comId) {
        loadedTrees += Constants.FIREBASE_PAGE_SIZE;
        syncProgress.setProgress(getProgressPercentage(loadedTrees));
        if (loadedTrees >= Constants.TOTAL_TREES) {
            loadComplete();
        } else {
            new Thread() {
                @Override
                public void run() {
                    SyncService service = new SyncService(Splash.this);
                    service.firebaseReload(comId);
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }

    }

    public void launchHome(View view) {
        startMapsActivity();
    }

    @Override
    public void loadComplete() {
        Constants.trees = Tree.findWithQuery(Tree.class, "SELECT * FROM TREE");
        Toast.makeText(this, "Data Loading Complete", Toast.LENGTH_SHORT).show();
        startMapsActivity();
    }
}
