package br.com.sd.go.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.requests.LastInfDeviceRequest;
import br.com.sd.go.utils.NetworkUtils;

public class MapFragment extends SupportMapFragment {

    private static final String TAG = MapFragment.class.getCanonicalName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void addMarker(Double lat, Double lng, String title, String snippet) {
        LatLng coordinate = new LatLng(lat, lng);
        getMap().addMarker(new MarkerOptions().position(coordinate).title(title).snippet(snippet));
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 10));
    }

    public void setMapType(int mapType) {
        getMap().setMapType(mapType);
    }

    public void clear() {
        getMap().clear();
    }

    public void updateView() {
        LastInfDeviceRequest request = new LastInfDeviceRequest
                (new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        clear();
                        for (int i = 0; i < response.length(); ++i) {
                            try {
                                JSONObject data = response.getJSONObject(i);
                                Double latitude = data.getDouble("latitude");
                                Double longitude = data.getDouble("longitude");
                                String name = data.getString("name");
                                String time = data.getString("time");

                                addMarker(latitude, longitude, name, time);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error while reading marker from response", e);
                            }
                        }
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

    private boolean isDrawerOpen() {
        return ((MainActivity) getActivity()).drawerIsOpen();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_sync);
        if (item != null) {
            item.setVisible(!isDrawerOpen());
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isAdded() && !isDrawerOpen()) {
            getActivity().getMenuInflater().inflate(R.menu.main, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sync:
                updateView();
                break;
            case R.id.action_map_type_normal:
                setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.action_map_type_satellite:
                setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.action_map_type_hybrid:
                setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.action_map_type_terrain:
                setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


}
