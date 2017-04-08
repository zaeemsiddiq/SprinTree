package monash.sprintree.service;

import android.os.AsyncTask;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import monash.sprintree.data.Constants;
import monash.sprintree.data.Tree;

/**
 * Created by Zaeem on 4/7/2017.
 */

public class WikimediaService extends AsyncTask<Void, Void, JSONObject> {

    private Tree tree;
    ImageView view;
    private WikimediaServiceComplete listener;

    public WikimediaService(WikimediaServiceComplete listener, ImageView view, Tree tree ) {
        this.listener = listener;
        this.view = view;
        this.tree = tree;
    }
    protected JSONObject doInBackground(Void... params) {
        return request("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=pageimages&redirects&titles="+tree.commonName+"&pithumbsize="+ Constants.WIKIPEDIA_IMAGE_SIZE+"");
    }
    private JSONObject request(String urlString) {

        StringBuffer chaine = new StringBuffer("");
        JSONObject result = new JSONObject();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }
        } catch (IOException e) {
            // Writing exception to log
            e.printStackTrace();
        }
        try {
            result = new JSONObject(String.valueOf(chaine));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    protected void onPostExecute(JSONObject result) {
        listener.wikiImageComplete(result, view);
    }
}
