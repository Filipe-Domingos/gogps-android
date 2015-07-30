package br.com.sd.go.fragments;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.StrictMode;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;

import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.models.VehicleItem;
import br.com.sd.go.requests.LastInfDeviceRequest;
import br.com.sd.go.requests.RouteByDeviceRequest;
import br.com.sd.go.utils.NetworkUtils;
import br.com.sd.go.utils.QuickReturnUtil;

public class GGMapFragment extends Fragment {

    private static View view;
    private GoogleMap mMap;
    private LinearLayout mBottomMenu;
    private VehicleItem mItem;

    private ProgressDialog mProgressDialog;

    public static final String ITEM_KEY = "item";
    public static final String SHOW_ROUTE_KEY = "show_route";

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
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        setHasOptionsMenu(true);

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

        String snippet = item.getTime() + " - " + item.getSpeed() + " km/h";

        MarkerOptions markerOptions = new MarkerOptions()
                .position(coordinate)
                .title(item.getName())
                .snippet(snippet)
                .icon(icon);

        mMap.addMarker(markerOptions);

        if (isLast) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 12));
        }
    }

    private void addMarkerForRoute(VehicleItem item, boolean isLast) {
        LatLng coordinate = new LatLng(item.getLatitude(), item.getLongitude());

        float color = item.getAcc() ? BitmapDescriptorFactory.HUE_GREEN :
                BitmapDescriptorFactory.HUE_RED;
        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(color);

        String snippet = item.getTime() + " - " + item.getSpeed() + " km/h";

        MarkerOptions markerOptions = new MarkerOptions()
                .position(coordinate)
                .title(item.getName())
                .snippet(snippet)
                .icon(icon);

        mMap.addMarker(markerOptions);

        if (isLast) {
            int zoom = 10; //mItem == null ? 10 : 15;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinate, zoom));
        }

    }

    private void drawArrowHead(GoogleMap mMap, LatLng from, LatLng to){
        // obtain the bearing between the last two points
        double bearing = GetBearing(from, to);

        // round it to a multiple of 3 and cast out 120s
        double adjBearing = Math.round(bearing / 3) * 3;
        while (adjBearing >= 120) {
            adjBearing -= 120;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Get the corresponding triangle marker from Google
        URL url;
        Bitmap image = null;

        try {
            url = new URL("http://www.google.com/intl/en_ALL/mapfiles/dir_" + String.valueOf((int)adjBearing) + ".png");
            try {
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (image != null){

            // Anchor is ratio in range [0..1] so value of 0.5 on x and y will center the marker image on the lat/long
            float anchorX = 0.5f;
            float anchorY = 0.5f;

            int offsetX = 0;
            int offsetY = 0;

            // images are 24px x 24px
            // so transformed image will be 48px x 48px

            //315 range -- 22.5 either side of 315
            if (bearing >= 292.5 && bearing < 335.5){
                offsetX = 24;
                offsetY = 24;
            }
            //270 range
            else if (bearing >= 247.5 && bearing < 292.5){
                offsetX = 24;
                offsetY = 12;
            }
            //225 range
            else if (bearing >= 202.5 && bearing < 247.5){
                offsetX = 24;
                offsetY = 0;
            }
            //180 range
            else if (bearing >= 157.5 && bearing < 202.5){
                offsetX = 12;
                offsetY = 0;
            }
            //135 range
            else if (bearing >= 112.5 && bearing < 157.5){
                offsetX = 0;
                offsetY = 0;
            }
            //90 range
            else if (bearing >= 67.5 && bearing < 112.5){
                offsetX = 0;
                offsetY = 12;
            }
            //45 range
            else if (bearing >= 22.5 && bearing < 67.5){
                offsetX = 0;
                offsetY = 24;
            }
            //0 range - 335.5 - 22.5
            else {
                offsetX = 12;
                offsetY = 24;
            }

            Bitmap wideBmp;
            Canvas wideBmpCanvas;
            Rect src, dest;

            // Create larger bitmap 4 times the size of arrow head image
            wideBmp = Bitmap.createBitmap(image.getWidth() * 2, image.getHeight() * 2, image.getConfig());

            wideBmpCanvas = new Canvas(wideBmp);

            src = new Rect(0, 0, image.getWidth(), image.getHeight());
            dest = new Rect(src);
            dest.offset(offsetX, offsetY);

            wideBmpCanvas.drawBitmap(image, src, dest, null);

            mMap.addMarker(new MarkerOptions()
                    .position(to)
                    .icon(BitmapDescriptorFactory.fromBitmap(wideBmp))
                    .anchor(anchorX, anchorY));
        }
    }

    private double GetBearing(LatLng from, LatLng to){
        double lat1 = from.latitude * Math.PI / 180.0;
        double lon1 = from.longitude * Math.PI / 180.0;
        double lat2 = to.latitude * Math.PI / 180.0;
        double lon2 = to.longitude * Math.PI / 180.0;

        // Compute the angle.
        double angle = - Math.atan2( Math.sin( lon1 - lon2 ) * Math.cos( lat2 ), Math.cos( lat1 ) * Math.sin( lat2 ) - Math.sin( lat1 ) * Math.cos( lat2 ) * Math.cos( lon1 - lon2 ) );

        if (angle < 0.0)
            angle += Math.PI * 2.0;

        // And convert result to degrees.
        double degreesPerRadian = 180.0 / Math.PI;
        angle = angle * degreesPerRadian;

        return angle;
    }

    public void setMapType(int mapType) {
        mMap.setMapType(mapType);
    }

    public void clear() {
        mMap.clear();
    }

    private void showRoute() {
        mProgressDialog = ProgressDialog.show(getActivity(), "Aguarde", "Carregando rota...");

        Long deviceId = mItem.getId();
        String date = android.text.format.DateFormat.format("yyyy-MM-d",
                new java.util.Date()).toString();

        String range = date + "%2000:00:00/" + date + "%2023:59:59";
        RouteByDeviceRequest request = new RouteByDeviceRequest
                (deviceId, range, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        clear();
                        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
                        LatLng lastPoint = null;

                        for (int i = 0; i < response.length(); ++i) {
                            try {
                                JSONObject data = response.getJSONObject(i);
                                Long id = data.getLong("device_id");
                                VehicleItem item = new VehicleItem(id, mItem.getName());

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
                                    addMarkerForRoute(item, i == response.length() - 1);

                                    LatLng point = new LatLng(latitude, longitude);
                                    options.add(point);

                                    if (lastPoint != null) {
                                        drawArrowHead(mMap, lastPoint, point);
                                    }

                                    lastPoint = point;
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Error while reading marker from response", e);
                            }
                        }

                        mMap.addPolyline(options);

                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.cancel();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.cancel();
                        }
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

    private void showPoint() {
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

    public void updateView() {
        if (getArguments() != null && getArguments().getBoolean(SHOW_ROUTE_KEY, false)) {
            ((MainActivity) getActivity()).setCustomTitle("Rota");
            showRoute();
        } else {
            showPoint();
        }
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
