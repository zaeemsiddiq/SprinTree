package monash.sprintree.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.FirebaseApp;

import monash.sprintree.R;
import monash.sprintree.service.SyncService;
import monash.sprintree.service.SyncServiceComplete;

public class Splash extends AppCompatActivity implements SyncServiceComplete{

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
        SyncService service = new SyncService(this);
        service.syncTrees();
        //test develop
        //Intent maps = new Intent(this, MapsActivity.class);
        //startActivity(maps);
    }

    @Override
    public void pageComplete(String comId) {
        // update the progress bar here

        SyncService service = new SyncService(this);
        service.firebaseReload(comId);
    }

    @Override
    public void loadComplete() {
        System.out.print("load complete");
    }
}
