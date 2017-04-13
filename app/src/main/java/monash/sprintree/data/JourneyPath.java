package monash.sprintree.data;

import com.orm.SugarRecord;

/**
 * Created by Zaeem on 4/14/2017.
 */

public class JourneyPath extends SugarRecord<JourneyPath> {
    public Journey journey;
    public double timestamp;
    public double latitude;
    public double longitude;
}
