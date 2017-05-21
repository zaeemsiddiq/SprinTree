package monash.sprintree.data;

import com.orm.SugarRecord;

/**
 * Created by Zaeem on 4/14/2017.
 */

public class JourneyTree extends SugarRecord<JourneyTree> {
    // Bridge relation between tree and journey,
    // each object can be represented as id (auto increment, part of sugarorm), journeyId, treeId in terms of relational database
    public Journey journey;
    public Tree tree;
}
