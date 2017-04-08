package monash.sprintree.service;

import android.media.Image;
import android.widget.ImageView;

import org.json.JSONObject;

/**
 * Created by Zaeem on 4/7/2017.
 */

public interface WikimediaServiceComplete {
    public void wikiImageComplete(JSONObject result, ImageView view);
}
