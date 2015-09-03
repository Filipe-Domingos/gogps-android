package br.com.sd.go.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.models.VehicleItem;
import br.com.sd.go.utils.NetworkUtils;

public class ListCarsAdapter extends ArrayAdapter<VehicleItem> {

    private static final String TAG = ListCarsAdapter.class.getSimpleName();

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
        final ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_list_cars, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.tvPlaca = (TextView) convertView.findViewById(R.id.tvPlaca);
            viewHolder.tvVelocidade = (TextView) convertView.findViewById(R.id.tvVelocidade);
            viewHolder.tvDistancia = (TextView) convertView.findViewById(R.id.tvDistancia);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        VehicleItem item = getItem(position);
        viewHolder.tvPlaca.setText(item.getName());

        if (item.getSpeed() == null) {
            viewHolder.tvVelocidade.setVisibility(View.GONE);
        } else {
            viewHolder.tvVelocidade.setVisibility(View.VISIBLE);
            viewHolder.tvVelocidade.setText(item.getSpeed() + " Km/h");
        }

        viewHolder.tvTime.setText(item.getTime());

        viewHolder.tvAddress.setText("Carregando endereço...");

        String latlong = item.getLatitude() + "," + item.getLongitude();
        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latlong;

        JsonObjectRequest request = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                String address = null;
                if (response.has("results")) {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        if (results.length() > 1) {
                            JSONObject result = results.getJSONObject(0);
                            address = result.optString("formatted_address", "Endereço não encontrado.");
                            address = address.replace(", Brazil", "");
                            address = address.replace(", Brasil", "");
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Erro ao ler endereço", e);
                    }
                }

                if (address == null) {
                    viewHolder.tvAddress.setText("Endereço não encontrado.");
                } else {
                    viewHolder.tvAddress.setText(address);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                viewHolder.tvAddress.setText("Endereço não encontrado.");
                error.printStackTrace();
            }
        });

        NetworkUtils.addToRequestQueue(request);
        return convertView;
    }

    private static class ViewHolder {
        TextView tvPlaca;
        TextView tvVelocidade;
        TextView tvDistancia;
        TextView tvAddress;
        TextView tvTime;
    }
}
