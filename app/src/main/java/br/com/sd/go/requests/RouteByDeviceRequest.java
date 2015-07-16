package br.com.sd.go.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import br.com.sd.go.GoGPS;

public class RouteByDeviceRequest extends JsonArrayRequest {

    private static final String sUrl = "http://gogps.com.br/gogps-rest/api/device/getRouter/:deviceId/:range";

    public RouteByDeviceRequest(Long deviceId, String range, Response.Listener<JSONArray> listener,
                                Response.ErrorListener errorListener) {
        super(getCustomUrl(deviceId, range), listener, errorListener);
    }

    private static String getCustomUrl(Long deviceId, String range) {
        return sUrl.replace(":deviceId", deviceId.toString()).replace(":range", range);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<>();

        String auth = GoGPS.getBasicAuth();
        if (auth != null) {
            params.put("Authorization", "Basic " + auth);
        }
        params.putAll(super.getHeaders());
        return params;
    }
}
