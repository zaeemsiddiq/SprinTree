package monash.sprintree.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.firebase.FirebaseApp;

import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.service.SyncService;
import monash.sprintree.service.SyncServiceComplete;

public class Splash extends AppCompatActivity implements SyncServiceComplete{

    /*
    View objects
     */
    ProgressBar syncProgress;

    /*
    Data objects
     */
    private int loadedTrees;

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

        SyncService service = new SyncService(this);
        service.syncTrees();
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
    }

    @Override
    public void pageComplete(String comId) {
        loadedTrees += Constants.FIREBASE_PAGE_SIZE;
        syncProgress.setProgress( getProgressPercentage(loadedTrees));
        SyncService service = new SyncService(this);
        service.firebaseReload(comId);
    }

    @Override
    public void loadComplete() {
        System.out.print("load complete");
    }
}
