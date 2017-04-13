package monash.sprintree.data;

import com.orm.SugarRecord;

/**
 * Created by Zaeem on 4/14/2017.
 */

public class JourneyTree extends SugarRecord<JourneyTree> {
    public Journey journey;
    public Tree tree;
}
