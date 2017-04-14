package monash.sprintree.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.ParcelFileDescriptor;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.orm.SugarRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import monash.sprintree.data.Tree;

/**
 * Created by Zaeem on 3/27/2017.
 */

public class Utils {


    public static void openRenderer(Context context,String fileName) throws IOException {
        File file=  FileUtils.fileFromAsset(context, fileName);
        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(file));
            Type listType = new TypeToken<List<Tree>>(){}.getType();
            List<Tree> posts = (List<Tree>) gson.fromJson(reader, listType);
            SugarRecord.saveInTx(posts);
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static class FileUtils {
        private FileUtils() {
        }

        private static File fileFromAsset(Context context, String assetName) throws IOException {
            File outFile = new File(context.getCacheDir(), assetName );
            copy(context.getAssets().open(assetName), outFile);

            return outFile;
        }

        private static void copy(InputStream inputStream, File output) throws IOException {
            FileOutputStream outputStream = null;

            try {
                outputStream = new FileOutputStream(output);
                boolean read = false;
                byte[] bytes = new byte[1024];

                int read1;
                while((read1 = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read1);
                }
            } finally {
                try {
                    if(inputStream != null) {
                        inputStream.close();
                    }
                } finally {
                    if(outputStream != null) {
                        outputStream.close();
                    }

                }

            }

        }
    }

    public static String loadJSONFromAsset( Context context ) throws JSONException {
        String json = null;
        try {
            InputStream is = context.getAssets().open("treeglossary.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            System.out.println(json);
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
        }
        return json;
    }

    public static void deleteDB(Context context) {
        String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/tree.db";
        File currentDB = new File(currentDBPath);
        if(currentDB.delete()) {
            System.out.println("Database deleted");
        } else {
            System.out.println("Database could not be deleted");
        }
    }

    public static void fullScreen(Activity activity) {
        activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    /*
    return unix timestamp
     */
    public static Long getCurrentTimeStamp() {
        return System.currentTimeMillis()/1000;
    }

    public static String getTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(new Date());
    }

}
