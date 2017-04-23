package monash.sprintree.fragments;

import monash.sprintree.data.Tree;

/**
 * Created by Zaeem on 4/2/2017.
 */

public interface FragmentListener {
    public void mapReady();
    public void mapButtonPressed( int buttonIdentifier );
    public boolean isTreeVisited(Tree tree);
    public void updateTimer(int hrs, int mins, int secs);
}
