package monash.sprintree.activities;

import cn.pedant.SweetAlert.SweetAlertDialog;
import monash.sprintree.data.Journey;
import monash.sprintree.data.JourneyPath;
import monash.sprintree.data.Tree;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.novoda.merlin.Merlin;
import com.novoda.merlin.MerlinsBeard;
import com.novoda.merlin.registerable.connection.Connectable;
import com.orm.SugarRecord;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import monash.sprintree.R;
import monash.sprintree.data.Constants;
import monash.sprintree.service.SyncService;
import monash.sprintree.service.SyncServiceComplete;
import monash.sprintree.utils.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashScreen extends AppCompatActivity implements SyncServiceComplete {

    /*
    View objects
     */
    ProgressBar syncProgress;

    /*
    Data objects
     */
    private int loadedTrees;
    private Merlin merlin;
    private MerlinsBeard merlinsBeard;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Lato-Light.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
        Utils.fullScreen(SplashScreen.this);
        setContentView(R.layout.activity_splash_screen);
        if (getIntent().getBooleanExtra("Exit me", false)) {
            finish();
        }
        initiateLayout();
        merlin = new Merlin.Builder().withConnectableCallbacks().build(this);
        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
            }
        });
        merlinsBeard = MerlinsBeard.from(this);
        if (merlinsBeard.isConnected()) {
            startLoading();
        } else {
            showDialog(); // ask user if they want to turn on their internet
        }
        //deleteDB();
        //Utils.exportDatabse( this );

        //startDashboardActivity();
        //test();
    }

    private void hideProgressViews() {
        findViewById(R.id.mainProgressBar).setVisibility(View.INVISIBLE);
        TextView textView = (TextView) findViewById(R.id.mainProgressText);
        textView.setText("Oops !!, I would be needing an internet connection to go ahead :(");
    }

    private void showProgressViews() {
        findViewById(R.id.mainProgressBar).setVisibility(View.VISIBLE);
        TextView textView = (TextView) findViewById(R.id.mainProgressText);
        textView.setText("Please wait while i load trees for you");
    }

    @Override
    protected void onResume() {
        super.onResume();
        merlin.bind();
    }

    @Override
    protected void onPause() {
        merlin.unbind();
        super.onPause();
    }

    private void showDialog() {
        hideProgressViews();
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
        dialog.setTitleText("Please turn on data services");
        dialog.setCancelText("Cancel");
        dialog.setConfirmText("Settings");
        dialog.showCancelButton(true);
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                startActivityForResult(new Intent(Settings.ACTION_SETTINGS), 0);   // got settings
                sweetAlertDialog.dismiss();
                sweetAlertDialog.cancel();
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0) {
            if (merlinsBeard.isConnected()) {
                startLoading();
            } else {
                showDialog(); // ask user if they want to turn on their internet
            }
        }
    }

    private void test() {
        List<Journey> journeys = Journey.listAll(Journey.class);
        List<JourneyPath> journeyPaths = journeys.get(0).getPath();
        System.out.println(journeys.size());
    }

    private void startLoading() {
        showProgressViews();
        new Thread() {
            @Override
            public void run() {
                final List<Tree> trees = Tree.findWithQuery(Tree.class, "SELECT * FROM TREE LIMIT 4000");
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
                                    openRenderer(getApplicationContext(), "structured2.json");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                startDashboardActivity();
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
        startActivity(mapIntent);
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
                    SyncService service = new SyncService(SplashScreen.this);
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

    @Override
    public void loadComplete() {
        Constants.trees = Tree.findWithQuery(Tree.class, "SELECT * FROM TREE");
        startMapsActivity();
    }

    public void openRenderer(final Context context, final String fileName) throws IOException {

        new Thread() {
            @Override
            public void run() {
                File file= null;
                try {
                    file = Utils.FileUtils.fileFromAsset(context, fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Gson gson = new Gson();
                    JsonReader reader = new JsonReader(new FileReader(file));
                    Type listType = new TypeToken<List<Tree>>(){}.getType();
                    List<Tree> posts = (List<Tree>) gson.fromJson(reader, listType);
                    SugarRecord.saveInTx(posts);
                    Constants.trees = posts;
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    // code runs in a thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startDashboardActivity();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void startDashboardActivity() {
        Intent mapIntent = new Intent(this, Dashboard.class);
        startActivity(mapIntent);
        finish();
    }
}
