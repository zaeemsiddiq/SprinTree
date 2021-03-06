package monash.sprintree.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.Environment;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.channels.FileChannel;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import monash.sprintree.data.Constants;
import monash.sprintree.data.Tree;

/**
 * Created by Zaeem on 3/27/2017.
 */

public class Utils {

    /*
    This class deals with reading and parsing the data from files,
    making an activity full screen,
    deals with timestamps
     */

    public static void openRenderer(final Context context, String fileName) throws IOException {

        File file=  FileUtils.fileFromAsset(context, fileName);
        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(file));
            Type listType = new TypeToken<List<Tree>>(){}.getType();
            List<Tree> posts = (List<Tree>) gson.fromJson(reader, listType);
            SugarRecord.saveInTx(posts);
            Constants.trees = posts;
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static class FileUtils {
        private FileUtils() {
        }

        public static File fileFromAsset(Context context, String assetName) throws IOException {
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

    public static void deleteDB(Context context) {
        String currentDBPath = "/data/data/" + context.getPackageName() + "/databases/tree.db";
        File currentDB = new File(currentDBPath);
        if(currentDB.delete()) {
            System.out.println("Database deleted");
        } else {
            System.out.println("Database could not be deleted");
        }
    }

    public static void exportDatabse(Context context) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            //if (sd.canWrite()) {
                String currentDBPath = "//data//"+context.getPackageName()+"//databases//tree.db";
                String backupDBPath = "backupname.db";
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            //}
        } catch (Exception e) {

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
    public static long getCurrentTimeStamp() {
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        return Long.valueOf(tsLong);
    }

    public static String getTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(new Date());
    }

    public  static String timestampToDate(long timestamp, String format) {
        try{
            Date date = new Date(timestamp*1000L); // *1000 is to convert seconds to milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat(format); // the format of your date
            sdf.setTimeZone(TimeZone.getDefault()); // give a timezone reference for formating (see comment at the bottom
            String formattedDate = sdf.format(date);
            System.out.println(formattedDate);
            return formattedDate;
        }catch (Exception e) {
        }
        return "";
    }

}
