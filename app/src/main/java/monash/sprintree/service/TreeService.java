package monash.sprintree.service;

import com.google.firebase.database.DataSnapshot;

import monash.sprintree.data.Tree;

/**
 * Created by Zaeem on 3/28/2017.
 */

class TreeService {

    static String saveTree(DataSnapshot dataSnapshot) {
        int addedTrees = 0;
        Object key = dataSnapshot.getKey();
        Object value = dataSnapshot.getValue();

        String comId = "";
        for( DataSnapshot tree: dataSnapshot.getChildren()) {
            comId = tree.getKey();
            for( DataSnapshot columns: tree.getChildren() ) {
                String attribute = columns.getKey();
                Object data = (Object)columns.getValue();
            }
        }
        return comId;   // return the
    }
}
