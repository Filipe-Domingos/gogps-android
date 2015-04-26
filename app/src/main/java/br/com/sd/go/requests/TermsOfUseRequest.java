package br.com.sd.go.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import br.com.sd.go.GoGPS;

public class TermsOfUseRequest extends JsonArrayRequest {

    private static final String sUrl = "http://gogps.com.br/gogps-rest/api/parametros/getParam/termoUso";

    public TermsOfUseRequest(Response.Listener<JSONArray> listener,
                             Response.ErrorListener errorListener) {
        super(sUrl, listener, errorListener);
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