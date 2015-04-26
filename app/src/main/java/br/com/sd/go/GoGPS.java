package br.com.sd.go;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import br.com.sd.go.utils.GoGPSConstants;

public class GoGPS extends Application {

    static final private String TAG = GoGPS.class.getSimpleName();

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        Log.i(TAG, "GoGPS app startup");
        Log.i(TAG, "Device: " + Build.MODEL + "(" + Build.BRAND + ")");
        Log.d(TAG, "CPU ABI: " + Build.CPU_ABI + "," + Build.CPU_ABI2);
        Log.d(TAG, "Display: " + Build.DISPLAY);
    }

    public static Context getContext() {
        return mContext;
    }

    public static String getBasicAuth() {
        SharedPreferences prefs = mContext.getSharedPreferences(GoGPSConstants.PREFS_FILE, 0);
        return prefs.getString(GoGPSConstants.PREFS_BASIC_AUTH, null);
    }

    @SuppressLint("CommitPrefEdits")
    public static void setBasicAuth(String basicAuth) {
        SharedPreferences prefs = mContext.getSharedPreferences(GoGPSConstants.PREFS_FILE, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(GoGPSConstants.PREFS_BASIC_AUTH, basicAuth);
        editor.commit();
    }
}