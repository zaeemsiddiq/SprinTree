package monash.sprintree.listAdapters;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import monash.sprintree.R;
import monash.sprintree.data.History;

/**
 * Created by Zaeem on 6/1/2016.
 */
public class HistoryListAdapter extends ArrayAdapter implements Filterable {
    List<History> historyList;
    List<History> originalHistoryList;
    private Context context;
    private SearchFilter searchFilter;

    private class ViewHolder {
        TextView HistoryID;
        TextView HistoryName;
    }
    public HistoryListAdapter(Context context, List<History> list) {
        super(context, android.R.layout.simple_dropdown_item_1line, list);
        this.historyList = list;
        this.originalHistoryList = list;
        this.context = context;
    }

    public History getItem(int position)
    {
        return historyList.get(position);
    }

    public int getCount() {
        if(historyList == null)
          return 0;
        return historyList.size();
    }

    public void resetData() {   // calling this method to reset the filterable data to original list which was initialised at start
        historyList = originalHistoryList;
    }

    @Override
    public Filter getFilter() {
        if (searchFilter == null)
            searchFilter = new SearchFilter();
        return searchFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder viewHolder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_history_item, null);
            TextView HistoryId = (TextView) v.findViewById(R.id.textViewFavoriteID);
            TextView HistoryName = (TextView) v.findViewById(R.id.textViewFavoriteName);

            viewHolder.HistoryID = HistoryId;
            viewHolder.HistoryName = HistoryName;
            v.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) v.getTag();

        History d = getItem(position);
        viewHolder.HistoryID.setText(Integer.toString(d.historyId));
        viewHolder.HistoryName.setText(d.historyName);
        return v;
    }

    private class SearchFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null || constraint.length() == 0) {
                results.values = historyList;
                results.count = historyList.size();
            }
            else {
                List<History> filteredList = new ArrayList<History>();
                for (History c : historyList) {
                    if (c.historyName.toUpperCase().contains(constraint.toString().toUpperCase()))
                        filteredList.add(c);
                }
                results.values = filteredList;
                results.count = filteredList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            historyList = (List<History>) results.values;
            notifyDataSetChanged();
        }

    }
}
