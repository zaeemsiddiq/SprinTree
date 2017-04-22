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


    public static void openRenderer(Context context,String fileName) throws IOException {
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
    public static Long getCurrentTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        java.util.Date currenTimeZone=new java.util.Date((long)1379487711*1000);
        return calendar.getTime().getTime();
    }

    public static String getTodaysDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(new Date());
    }

    public  static String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar calendar = Calendar.getInstance();
            TimeZone tz = TimeZone.getDefault();
            calendar.setTimeInMillis(timestamp * 1000);
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date currenTimeZone = (Date) calendar.getTime();
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }

}
