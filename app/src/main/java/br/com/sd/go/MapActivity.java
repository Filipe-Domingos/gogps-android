package br.com.sd.go;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MapActivity extends Activity {

    private GoogleMap map;
    private ProgressDialog progressDialog;
    private Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle args = getIntent().getExtras();

        map  = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        /** Recupera o argumento de qual metodo foi clicado */
        if (args != null) {
            this.id = args.getInt("id");
            sync();
        }
    }

    private void sync() {
        if(isConnected()){
            map.clear();
            new HttpAsyncTask(this).execute("http://gogps.com.br/positions/last/"+id);
        } else {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void addMarker(double lat, double lng, String title, String snippet) {
        LatLng coordenate = new LatLng(lat, lng);
        map.addMarker(new MarkerOptions().position(coordenate).title(title).snippet(snippet));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(coordenate, 10));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int mapType;

        switch (item.getItemId()) {
            case R.id.action_settings :
                sync();
            case R.id.action_map_type_normal :
                mapType = GoogleMap.MAP_TYPE_NORMAL;
                break;
            case R.id.action_map_type_satellite :
                mapType = GoogleMap.MAP_TYPE_SATELLITE;
                break;
            case R.id.action_map_type_hybrid :
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                break;
            case R.id.action_map_type_terrain :
                mapType = GoogleMap.MAP_TYPE_TERRAIN;
                break;
            default :
                return super.onOptionsItemSelected(item);
        }

        map.setMapType(mapType);
        return true;
    }


    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {
            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        Context context;

        public HttpAsyncTask(Context context) {
            this.context = context;
        }
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {
                JSONArray jsonArray = new JSONArray(result);

                for(int i = 0; i<jsonArray.length(); i++) {
                    JSONObject json = (JSONObject) jsonArray.get(i);
                    addMarkers(json);
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("error", e.getMessage());
                Toast.makeText(getBaseContext(), R.string.falha_local, Toast.LENGTH_LONG).show();
            } finally {
                hideProgressDialog();
            }
        }
    }

    private void addMarkers(JSONObject json) {
        try {
            addMarker(json.getDouble("latitude"), json.getDouble("longitude"), json.getString("name"), json.getString("time"));
        } catch (JSONException e) {
             e.printStackTrace();
             Log.e("error", e.getMessage());
        }
    }


    public void showProgressDialog() {
        try {
            progressDialog = ProgressDialog.show(this, "Aguarde...", this.getString(R.string.sync));
        } catch (Throwable e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    public void hideProgressDialog() {
        try {

            if(progressDialog != null) {
                progressDialog.dismiss();
            }

        } catch (Throwable e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }

}
