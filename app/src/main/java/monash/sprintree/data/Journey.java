package monash.sprintree.data;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by Zaeem on 4/14/2017.
 */

public class Journey extends SugarRecord<Journey> {

    public Long timestamp;
    public String date;
    public long score;
    public long distance;   //meters
    public long duration;   //seconds
    public String journeyImagePath; // path of image in local database

    public List<JourneyPath> getPath() {
        return JourneyPath.find(JourneyPath.class, "journey = ?",  getId().toString());
    }

    public List<JourneyTree> getTrees() {
        return JourneyTree.find(JourneyTree.class, "journey = ?",  getId().toString());
    }
}
