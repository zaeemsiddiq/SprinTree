package monash.sprintree.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.lylc.widget.circularprogressbar.CircularProgressBar;
import com.orm.SugarRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import at.markushi.ui.CircleButton;
import cn.pedant.SweetAlert.SweetAlertDialog;
import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Journey;
import monash.sprintree.data.JourneyPath;
import monash.sprintree.data.Tree;
import monash.sprintree.service.SyncService;
import monash.sprintree.service.SyncServiceComplete;
import monash.sprintree.utils.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Dashboard extends AppCompatActivity {

    /*
    This class opens up MapsActivity and selects a fragment based on user's choice.
     */

    /*
    View objects
     */

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        Utils.fullScreen(Dashboard.this);
        setContentView(R.layout.activity_dashboard);

        if (getIntent().getBooleanExtra("Exit me", false)) {
            finish();
        }
    }
    private void startMapsActivity(int tab) {
        Intent mapIntent = new Intent(this, MapsActivity.class);
        mapIntent.putExtra("showTab", tab);
        startActivityForResult(mapIntent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {   // this is fired if user presses the back button. its a good idea to ask the user before quitting the app

        try {
            new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("Do you want to exit ?")
                    .setCancelText("No")
                    .setConfirmText("Yes")
                    .showCancelButton(true)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            setResult(Constants.REQUEST_EXIT);
                            finish();
                            sDialog.cancel();
                        }
                    })
                    .show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // launch by passing the tab identifier, 0 is map tab, 1 is history tab and 2 is myTree tab

    public void launchMap(View view) {
        startMapsActivity(0);
    }

    public void launchHistory(View view) {
        startMapsActivity(1);
    }

    public void launchMyForest(View view) {
        startMapsActivity(2);
    }
}
