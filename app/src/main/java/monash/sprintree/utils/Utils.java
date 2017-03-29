package monash.sprintree.utils;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Zaeem on 3/27/2017.
 */

public class Utils {
    private void fullScreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public String loadJSONFromAsset( ) throws JSONException {
        String json = null;
        /*try {
            InputStream is = getAssets().open("test.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer)
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        JSONObject test = new JSONObject(json);
        JSONArray data = test.getJSONArray("data");
        for (int i = 0; i < data.length(); i++) {
            JSONArray tree = data.getJSONArray(i);
            String name = tree.get(9).toString();
            System.out.println(name);
        }*/
        return json;

    }
}
