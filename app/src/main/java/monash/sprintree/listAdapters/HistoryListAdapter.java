package monash.sprintree.listAdapters;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import monash.sprintree.R;
import monash.sprintree.data.Journey;
import monash.sprintree.utils.Utils;

/**
 * Created by Zaeem on 6/1/2016.
 */
public class HistoryListAdapter extends ArrayAdapter<Journey> implements Filterable {
    List<Journey> historyList;
    private Context context;

    private class ViewHolder {
        TextView TreeScore;
        TextView JourneyDate;
        TextView JourneyTime;
        ImageView WalkLogo;
    }
    public HistoryListAdapter(Context context, List<Journey> list) {
        super(context, R.layout.list_history_item, list);
        this.historyList = list;
        this.context = context;
    }

    public Journey getItem(int position)
    {
        return historyList.get(position);
    }

    public int getCount() {
        if(historyList == null)
          return 0;
        return historyList.size();
    }

    public void sortData( List<Journey> list ) {   // calling this method to reset the filterable data to original list which was initialised at start
        historyList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_history_item, null);
            TextView TreeScore = (TextView) v.findViewById(R.id.score);
            TextView JourneyDate = (TextView) v.findViewById(R.id.journeyDate);
            TextView JourneyTime = (TextView) v.findViewById(R.id.duration);
            ImageView WalkLogo = (ImageView) v.findViewById(R.id.walkIcon);

            viewHolder.TreeScore = TreeScore;
            viewHolder.JourneyDate = JourneyDate;
            viewHolder.JourneyTime =  JourneyTime;
            viewHolder.WalkLogo = WalkLogo;
            v.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) v.getTag();

        Journey d = getItem(position);
        viewHolder.JourneyTime.setText(Utils.getDateCurrentTimeZone(d.timestamp));
        viewHolder.JourneyDate.setText(d.date);
        viewHolder.TreeScore.setText(Long.toString(d.score));
        return v;
    }
}
