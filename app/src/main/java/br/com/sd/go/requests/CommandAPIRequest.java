package br.com.sd.go.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import br.com.sd.go.GoGPS;

public class CommandAPIRequest extends JsonObjectRequest {

    private static final String sUrl = "http://gogps.com.br/gogps-rest/api/command";

    public static final int BLOCK_COMMAND = 1;
    public static final int UNLOCK_COMMAND = 2;

    public CommandAPIRequest(JSONObject object, Response.Listener<JSONObject> listener,
                             Response.ErrorListener errorListener) {
        super(Method.POST, sUrl, object, listener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> params = new HashMap<>();

        String auth = GoGPS.getBasicAuth();
        if (auth != null) {
            params.put("Authorization", "Basic " + auth);
        }

        params.put("Content-Type", "application/x-www-form-urlencoded");

        params.putAll(super.getHeaders());
        return params;
    }
}