package monash.sprintree.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import monash.sprintree.R;

public class Splash extends AppCompatActivity {

    private void fullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //test develop
        //Intent maps = new Intent(this, MapsActivity.class);
        //startActivity(maps);
    }

}
