package monash.sprintree.fragments;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import monash.sprintree.R;
import monash.sprintree.activities.MapsActivity;
import monash.sprintree.activities.Statistics;
import monash.sprintree.data.Constants;
import monash.sprintree.data.Journey;
import monash.sprintree.data.JourneyPath;
import monash.sprintree.data.JourneyTree;
import monash.sprintree.listAdapters.HistoryListAdapter;

import static monash.sprintree.R.*;

public class HistoryFragment extends Fragment {
    private TextView errorMessageText;
    private ListView listView;
    MapsActivity listener;  // used to reference back to main activity
    private HistoryListAdapter historyListAdapter; // adapter to load into list
    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(MapsActivity listener) {
        HistoryFragment fragment = new HistoryFragment();
        fragment.listener = listener;   //setting up the listener
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(layout.fragment_history, container, false);
        initialiseListView(view);
        final Button sortByScore = (Button) view.findViewById(id.sortScoreButton);
        final Button sortByDate = (Button) view.findViewById(id.sortDateButton);

        sortByScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByDate.setBackground(getResources().getDrawable(drawable.button_border_right));
                sortByDate.setTextColor(getResources().getColor(color.kopie_blue));
                sortByScore.setTextColor(getResources().getColor(color.white));
                sortByScore.setBackground(getResources().getDrawable(drawable.button_pressed));
                historyListAdapter.sortData(Journey.findWithQuery(Journey.class, "SELECT * FROM JOURNEY ORDER BY SCORE DESC"));
                historyListAdapter.notifyDataSetChanged();
            }
        });

        //sortByDate.setBackground(getResources().getDrawable(R.drawable.buttonselector));
        sortByDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortByScore.setBackground(getResources().getDrawable(drawable.button_border));
                sortByScore.setTextColor(getResources().getColor(color.kopie_blue));
                sortByDate.setTextColor(getResources().getColor(color.white));
                sortByDate.setBackground(getResources().getDrawable(drawable.button_pressed_right));
               // sortByDate.setBackgroundColor(getResources().getColor(R.color.white));
                historyListAdapter.sortData(Journey.findWithQuery(Journey.class, "SELECT * FROM JOURNEY ORDER BY TIMESTAMP DESC"));
                historyListAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }
    /*
    public void refresh() { // reload the list
        historyListAdapter = new HistoryListAdapter(getActivity(), Constants.dbContext.getAllFavorites());
        listView.setAdapter(favoriteListAdapter);
        if(favoriteListAdapter.getCount()==0) { // hide/ show the error message
            listView.setVisibility(View.GONE);
            errorMessageText.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            errorMessageText.setVisibility(View.GONE);
        }
        favoriteListAdapter.notifyDataSetChanged();
    }*/
    private void initialiseListView(View view) {
        errorMessageText = (TextView) view.findViewById(id.favoritesTextEmptyMessage);
        errorMessageText.setVisibility(View.GONE);
        listView = (ListView) view.findViewById(id.journeyHistory);

        historyListAdapter = new HistoryListAdapter(getActivity(), Journey.listAll(Journey.class));
        listView.setAdapter(historyListAdapter);
        if(historyListAdapter.getCount()==0) { // hide/ show the error message
            listView.setVisibility(View.GONE);
            errorMessageText.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            errorMessageText.setVisibility(View.GONE);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {    // preplan
                Journey journey = historyListAdapter.getItem(position);
                List<JourneyPath> journeyPaths = journey.getPath();
                List<JourneyTree> journeyTrees = journey.getTrees();

                Intent intent = new Intent(getActivity(), Statistics.class);
                intent.putExtra( "journeyId", journey.getId() );
                Constants.jumpingToAnotherActivity = true;
                startActivityForResult(intent, 0);

                /*new AlertDialog.Builder(getActivity())
                        .setTitle("Confirm")
                        .setMessage("Are you sure you want to travel this route ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Favorite favorite = favoriteListAdapter.getItem(position);
                                listener.PrePlanStart(favorite.getSourceStop(), favorite.getDestinationStop());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();*/
            }
        });

       listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
           @Override
           public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

               return true;
           }
       });
    }
}
