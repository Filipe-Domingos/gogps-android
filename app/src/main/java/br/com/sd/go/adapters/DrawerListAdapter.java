package br.com.sd.go.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import br.com.sd.go.models.ItemMenuDrawer;
import br.com.sd.go.R;

public class DrawerListAdapter extends BaseAdapter {

    private Context mContext;
    private List<ItemMenuDrawer> mItems;

    public DrawerListAdapter(Context context, List<ItemMenuDrawer> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_drawer, null);
        }

        TextView tv = (TextView) view.findViewById(R.id.label);
        tv.setText(mItems.get(position).getTitle());

        Integer resourceImage = mItems.get(position).getResourceImage();
        ImageView iv = (ImageView) view.findViewById(R.id.image);
        if (resourceImage != null) {
            iv.setImageResource(mItems.get(position).getResourceImage());
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.INVISIBLE);
        }

        return view;
    }
}
