package br.com.sd.go.fragments;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.adapters.ListCarsAdapter;
import br.com.sd.go.models.VehicleItem;
import br.com.sd.go.requests.CommandAPIRequest;
import br.com.sd.go.requests.DevicesUserRequest;
import br.com.sd.go.swipemenu.SwipeMenu;
import br.com.sd.go.swipemenu.SwipeMenuCreator;
import br.com.sd.go.swipemenu.SwipeMenuItem;
import br.com.sd.go.swipemenu.SwipeMenuListView;
import br.com.sd.go.utils.CommandRequests;
import br.com.sd.go.utils.NetworkUtils;
import br.com.sd.go.utils.QuickReturnUtil;

public class ListCarsFragment extends Fragment {

    private static final String TAG = ListCarsFragment.class.getCanonicalName();

    private List<VehicleItem> mItems;
    private SwipeMenuListView mListView;
    private LinearLayout mBottomMenu;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = View.inflate(getActivity(), R.layout.activity_list, null);
        mItems = new ArrayList<>();
        mListView = (SwipeMenuListView) view.findViewById(R.id.listView);

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                SwipeMenuItem blockItem = new SwipeMenuItem(getActivity());
                blockItem.setBackground(new ColorDrawable(Color.rgb(0xFD, 0xFD, 0xFD)));
                blockItem.setWidth(dp2px(60));
                blockItem.setIcon(R.drawable.ic_action_lock_outline);

                menu.addMenuItem(blockItem);

                SwipeMenuItem unlockItem = new SwipeMenuItem(getActivity());
                unlockItem.setBackground(new ColorDrawable(Color.rgb(0xFD, 0xFD, 0xFD)));
                unlockItem.setWidth(dp2px(60));
                unlockItem.setIcon(R.drawable.ic_action_lock_open);

                menu.addMenuItem(unlockItem);

                SwipeMenuItem actualPositionItem = new SwipeMenuItem(getActivity());
                actualPositionItem.setBackground(new ColorDrawable(Color.rgb(0xFD, 0xFD, 0xFD)));
                actualPositionItem.setWidth(dp2px(60));
                actualPositionItem.setIcon(R.drawable.ic_maps_pin_drop);

                menu.addMenuItem(actualPositionItem);

                SwipeMenuItem routesItem = new SwipeMenuItem(getActivity());
                routesItem.setBackground(new ColorDrawable(Color.rgb(0xFD, 0xFD, 0xFD)));
                routesItem.setWidth(dp2px(60));
                routesItem.setIcon(R.drawable.ic_maps_map);

                menu.addMenuItem(routesItem);
            }

        };

        mListView.setMenuCreator(creator);

        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                VehicleItem item = mItems.get(position);
                MainActivity activity = (MainActivity) getActivity();
                Integer commandCode;
                switch (index) {
                    case 0:
                        commandCode = CommandAPIRequest.BLOCK_COMMAND;
                        new CommandRequests(activity, item.getId(), commandCode).sendCommand();
                        break;
                    case 1:
                        commandCode = CommandAPIRequest.UNLOCK_COMMAND;
                        new CommandRequests(activity, item.getId(), commandCode).sendCommand();
                        break;
                    case 2:
                        ((MainActivity) getActivity()).showCarInMap(item);
                        break;
                    case 3:
                        ((MainActivity) getActivity()).showCarRoute(item);
                }
                return false;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListView.smoothOpenMenu(position);
            }
        });

        loadData();

        mBottomMenu = ((LinearLayout) view.findViewById(R.id.bottom_menu));
        mBottomMenu.removeAllViews();

        Dictionary<Integer, View> options = QuickReturnUtil.getOptionsMenu(getActivity());

        View refresh = options.get(QuickReturnUtil.REFRESH_ITEM);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });
        mBottomMenu.addView(refresh);

        View showMap = options.get(QuickReturnUtil.MAP_ITEM);
        showMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).openMapFragment();
            }
        });
        mBottomMenu.addView(showMap);

        return view;
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
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

                                VehicleItem item = new VehicleItem(id, name);

                                Double latitude = data.getDouble("latitude");
                                Double longitude = data.getDouble("longitude");
                                item.setLatitude(latitude);
                                item.setLongitude(longitude);

                                if (data.has("other")) {
                                    String extraInfo = data.getString("other");
                                    item.setExtraInfo(extraInfo);
                                }

                                if (data.has("speed")) {
                                    String speed = data.getString("speed");
                                    item.setSpeed(speed);
                                }

                                mItems.add(item);
                            } catch (JSONException e) {
                                Log.e(TAG, "Error while reading marker from response", e);
                            }
                        }

                        Collections.sort(mItems);

                        mListView.setAdapter(new ListCarsAdapter(getActivity(), mItems));
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
