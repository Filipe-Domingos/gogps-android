package br.com.sd.go;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.sd.go.adapters.DrawerListAdapter;
import br.com.sd.go.fragments.GGMapFragment;
import br.com.sd.go.fragments.ListCarsFragment;
import br.com.sd.go.fragments.TermsFragment;
import br.com.sd.go.models.ItemMenuDrawer;
import br.com.sd.go.models.VehicleItem;

public class MainActivity extends ActionBarActivity implements ListView.OnItemClickListener {

    private ActionBar actionBar;
    private DrawerLayout drawerLayout;
    private ListView listView;
    private ActionBarDrawerToggle barTg;

    private CharSequence titleDrawer;
    private CharSequence titleFragment;

    public final static int CARS_POSITION = 1;
    public final static int TERMS_MENU_POSITION = 2;
    public final static int ROUTE_POSITION = 3;
    public final static int MAP_POSITION = 4;

    Handler updateTimeHandler = new Handler();
    Runnable runnableUpdateTime = new Runnable() {
        public void run() {
            supportInvalidateOptionsMenu();
            updateTimeHandler.postDelayed(this, 3000);
        }
    };

    TextView timeViewBar;

    private List<ItemMenuDrawer> mItemsDrawer = new ArrayList<ItemMenuDrawer>() {{
        add(new ItemMenuDrawer("Meus Veículos", R.drawable.ic_my_cars));
        add(new ItemMenuDrawer("Termos de uso", R.drawable.ic_action_terms_of_use));
        add(new ItemMenuDrawer("Sair", R.drawable.ic_action_exit));
        //        add(new ItemMenuDrawer("Rastreamento", R.drawable.ic_device_gps_fixed));
    }};

    private int mActualPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawer_layout);

        titleDrawer = titleFragment = getTitle();

        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        DrawerListAdapter drawerAdapter = new DrawerListAdapter(this, mItemsDrawer);

        listView = (ListView) findViewById(R.id.lv_navigator);
        listView.setOnItemClickListener(this);

        View header = LayoutInflater.from(this).inflate(R.layout.header_menu,
                listView, false);
        listView.addHeaderView(header, null, false);

        listView.setAdapter(drawerAdapter);

        drawerLayout = (DrawerLayout) findViewById(R.id.dl);

        barTg = new ActionBarDrawerToggle(this, drawerLayout,
                R.drawable.toggle_img, R.drawable.toggle_img) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                actionBar.setTitle(titleFragment);
                supportInvalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(titleDrawer);
                supportInvalidateOptionsMenu();
                syncState();
            }
        };

        drawerLayout.setDrawerListener(barTg);
        barTg.syncState();

        if (savedInstanceState == null) {
            selectedItem(1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateTimeHandler.removeCallbacks(runnableUpdateTime);
        if (isFinishing() && !GoGPS.getRemember()) {
            GoGPS.setBasicAuth(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            String text = android.text.format.DateFormat.format("d/MM hh:mm  ",
                    new java.util.Date()).toString();

            timeViewBar = new TextView(this);
            timeViewBar.setText(text);
            timeViewBar.setTextColor(getResources().getColor(R.color.white));
            timeViewBar.setOnClickListener(null);
            timeViewBar.setPadding(5, 5, 5, 5);
            timeViewBar.setTextSize(16);
            menu.add(0, R.string.app_name, 1, R.string.app_name)
                    .setActionView(timeViewBar)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            updateTimeHandler.postDelayed(runnableUpdateTime, 1000);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (drawerLayout.isDrawerOpen(listView)) {
                    drawerLayout.closeDrawer(listView);
                } else {
                    drawerLayout.openDrawer(listView);
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        barTg.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        barTg.onConfigurationChanged(newConfig);
    }

    public void showCarInMap(VehicleItem item) {
        selectedItem(MAP_POSITION, item);
    }

    public void showCarRoute(VehicleItem item) {
        Fragment frag = new GGMapFragment();
        if (item != null) {
            Bundle args = new Bundle();
            args.putSerializable(GGMapFragment.ITEM_KEY, item);
            args.putBoolean(GGMapFragment.SHOW_ROUTE_KEY, true);
            frag.setArguments(args);
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.frame_layout, frag);
        ft.commit();

        mActualPosition = ROUTE_POSITION;
    }

    public void openMapFragment() {
        selectedItem(MAP_POSITION);
    }

    public void openCarsFragment() {
        selectedItem(CARS_POSITION);
    }

    public void openTermsFragment() {
        selectedItem(TERMS_MENU_POSITION);
    }

    private void selectedItem(int position, VehicleItem item) {
        --position; // Por conta do header que foi adicionado e conta como uma posição.

        if (position == mActualPosition) {
            drawerLayout.closeDrawer(listView);
            return;
        }

        FragmentTransaction ft;
        Fragment frag = null;

        switch (position) {
            case 0:
                frag = new ListCarsFragment();
                break;
            case 1:
                frag = new TermsFragment();
                break;
            case 2:
                GoGPS.setBasicAuth(null);
                startActivity(new Intent(getBaseContext(), LoginActivity.class));
                finish();
                break;
            case 3:
                frag = new GGMapFragment();
                if (item != null) {
                    Bundle args = new Bundle();
                    args.putSerializable(GGMapFragment.ITEM_KEY, item);
                    frag.setArguments(args);
                }
                break;
            default:
                break;
        }

        if (frag != null) {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.frame_layout, frag);
            ft.commit();
        }

        listView.setItemChecked(position, true);
        String title = "Go! GPS";
        if (mItemsDrawer.size() > position) {
            title = mItemsDrawer.get(position).getTitle();
        } else if (item != null) {
            title = item.getName();
        }
        setCustomTitle(title);
        drawerLayout.closeDrawer(listView);

        mActualPosition = position;
    }

    private void selectedItem(int position) {
        selectedItem(position, null);
    }

    public void setCustomTitle(String title) {
        actionBar.setTitle(title);
        titleFragment = title;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedItem(position);
    }

    public boolean drawerIsOpen() {
        return drawerLayout.isDrawerOpen(listView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && "LGE".equalsIgnoreCase(Build.BRAND)) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
