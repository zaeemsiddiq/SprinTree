package monash.sprintree.data;

import com.orm.SugarRecord;

/**
 * Created by Zaeem on 4/14/2017.
 */

public class JourneyPath extends SugarRecord<JourneyPath> {
    // connected by a journeyId and has other fields
    public Journey journey;
    public double timestamp;
    public double latitude;
    public double longitude;
}
