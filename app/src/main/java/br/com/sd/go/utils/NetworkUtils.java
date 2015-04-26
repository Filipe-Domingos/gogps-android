package br.com.sd.go.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import br.com.sd.go.GoGPS;

public class NetworkUtils {

    public static boolean hasConnection(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private static RequestQueue requestQueue = null;
    private static final Object lock = new Object();

    public static RequestQueue getRequestQueue(Context context) {
        synchronized (lock) {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(context);
            }
        }
        return requestQueue;
    }

    public static <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue(GoGPS.getContext()).add(req);
    }

}