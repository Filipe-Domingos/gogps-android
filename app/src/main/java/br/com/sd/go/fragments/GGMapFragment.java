package br.com.sd.go.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Dictionary;

import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.models.VehicleItem;
import br.com.sd.go.requests.LastInfDeviceRequest;
import br.com.sd.go.utils.NetworkUtils;
import br.com.sd.go.utils.QuickReturnUtil;

public class GGMapFragment extends Fragment {

    private static View view;
    private GoogleMap mMap;
    private LinearLayout mBottomMenu;
    private VehicleItem mItem;

    public static final String ITEM_KEY = "item";

    private static final String TAG = GGMapFragment.class.getCanonicalName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = View.inflate(getActivity(), R.layout.activity_main, null);
        } catch (InflateException e) {
            Log.e(TAG, "View já adicionada");
        }

        mMap = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        setHasOptionsMenu(false);

        mBottomMenu = ((LinearLayout) view.findViewById(R.id.bottom_menu));
        mBottomMenu.removeAllViews();

        Dictionary<Integer, View> options = QuickReturnUtil.getOptionsMenu(getActivity());

        View refresh = options.get(QuickReturnUtil.REFRESH_ITEM);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateView();
            }
        });
        mBottomMenu.addView(refresh);

        View apps = options.get(QuickReturnUtil.APPS_ITEM);
        apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).openCarsFragment();
            }
        });
        mBottomMenu.addView(apps);

        if (getArguments() != null && getArguments().containsKey(ITEM_KEY)) {
            mItem = (VehicleItem) getArguments().getSerializable(ITEM_KEY);
            addMarker(mItem, true);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    private void addMarker(VehicleItem item, boolean isLast) {
        LatLng coordinate = new LatLng(item.getLatitude(), item.getLongitude());

        float color = item.getAcc() ? BitmapDescriptorFactory.HUE_GREEN :
                BitmapDescriptorFactory.HUE_RED;
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(color);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(coordinate)
                .title(item.getName())
                .snippet(item.getTime())
                .icon(icon);

        mMap.addMarker(markerOptions);

        if (isLast) {
            int zoom = mItem == null ? 10 : 15;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, zoom));
        }
    }

    public void setMapType(int mapType) {
        mMap.setMapType(mapType);
    }

    public void clear() {
        mMap.clear();
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
                                Long id = data.getLong("id");
                                String name = data.getString("name");
                                VehicleItem item = new VehicleItem(id, name);

                                Double latitude = data.getDouble("latitude");
                                Double longitude = data.getDouble("longitude");
                                item.setLatitude(latitude);
                                item.setLongitude(longitude);

                                String time = data.getString("time");
                                item.setTime(time);

                                if (data.has("other")) {
                                    String extraInfo = data.getString("other").replace("\\/", "/");
                                    item.setExtraInfo(extraInfo);
                                }

                                if (data.has("speed")) {
                                    String speed = data.getString("speed");
                                    item.setSpeed(speed);
                                }

                                if (mItem == null || mItem.getId().equals(item.getId())) {
                                    addMarker(item, i == response.length() - 1);
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error while reading marker from response", e);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message;
                        try {
                            String body = new String(error.networkResponse.data);
                            JSONObject response = new JSONObject(body);
                            message = response.getString("response");
                        } catch (Exception e) {
                            message = "Verifique sua conexão.";
                        }
                        if (isAdded()) {
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
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
