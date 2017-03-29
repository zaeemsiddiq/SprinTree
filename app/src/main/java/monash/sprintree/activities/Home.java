package monash.sprintree.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import monash.sprintree.R;

public class Home extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void launchMapScreen(View view) {
        Intent maps = new Intent(this, MapsActivity.class);
        startActivity(maps);
    }
}
