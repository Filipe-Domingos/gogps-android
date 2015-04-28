package br.com.sd.go.fragments;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.sd.go.adapters.ListCarsAdapter;
import br.com.sd.go.models.VehicleItem;
import br.com.sd.go.requests.DevicesUserRequest;
import br.com.sd.go.utils.NetworkUtils;

public class ListCarsFragment extends ListFragment {

    private static final String TAG = ListCarsFragment.class.getCanonicalName();

    private List<VehicleItem> mItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItems = new ArrayList<>();
        loadData();
    }

    private void loadData() {
        DevicesUserRequest request = new DevicesUserRequest
                (new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        mItems.clear();

                        for (int i = 0; i < response.length(); ++i) {
                            try {
                                JSONObject data = response.getJSONObject(i);
                                Long id = data.getLong("id");
                                String name = data.getString("name");

                                mItems.add(new VehicleItem(id, name));
                            } catch (JSONException e) {
                                Log.e(TAG, "Error while reading marker from response", e);
                            }
                        }

                        Collections.sort(mItems);

                        setListAdapter(new ListCarsAdapter(getActivity(), mItems));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Verifique sua conexão.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        NetworkUtils.addToRequestQueue(request);
    }
}
