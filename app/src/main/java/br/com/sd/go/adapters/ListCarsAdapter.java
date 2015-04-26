package br.com.sd.go.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.sd.go.R;
import br.com.sd.go.models.VehicleItem;

public class ListCarsAdapter extends ArrayAdapter<VehicleItem> {

    public ListCarsAdapter(Context context, List<VehicleItem> objects) {
        super(context, R.layout.item_list_cars, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if(convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_list_cars, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            convertView.setTag(viewHolder);
        } else {
            // recycle the already inflated view
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // update the item view
        VehicleItem item = getItem(position);
        viewHolder.tvTitle.setText(item.getName());

        return convertView;
    }

    private static class ViewHolder {
        TextView tvTitle;
    }
}
