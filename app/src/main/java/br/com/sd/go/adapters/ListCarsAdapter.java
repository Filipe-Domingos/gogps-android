package br.com.sd.go.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.models.VehicleItem;

public class ListCarsAdapter extends ArrayAdapter<VehicleItem> {

    public ListCarsAdapter(Context context, List<VehicleItem> objects) {
        super(context, R.layout.item_list_cars, objects);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        VehicleItem item = getItem(position);
        return item.getAcc() ? 1 : 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_list_cars, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.tvPlaca = (TextView) convertView.findViewById(R.id.tvPlaca);
            viewHolder.tvVelocidade = (TextView) convertView.findViewById(R.id.tvVelocidade);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        VehicleItem item = getItem(position);
        viewHolder.tvPlaca.setText(item.getName());

        if (item.getSpeed() == null) {
            viewHolder.tvVelocidade.setVisibility(View.GONE);
        } else {
            viewHolder.tvVelocidade.setVisibility(View.GONE);
            viewHolder.tvVelocidade.setText(item.getSpeed() + " Km/h");
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView tvPlaca;
        TextView tvVelocidade;
    }
}
