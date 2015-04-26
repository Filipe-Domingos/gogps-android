package br.com.sd.go.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import br.com.sd.go.GoGPS;

public class LoginRequest extends StringRequest {

    private static final String sUrl = "http://gogps.com.br/gogps-rest/api/user/login";
    private String mUser;
    private String mPassword;

    public LoginRequest(String user, String password, Response.Listener<String> listener,
                        Response.ErrorListener errorListener) {
        super(sUrl, listener, errorListener);
        mUser = user;
        mPassword = password;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> pars = new HashMap<>();
        pars.put("Content-Type", "application/x-www-form-urlencoded");
        return pars;
    }

    @Override
    public Map<String, String> getParams() throws AuthFailureError {
        Map<String, String> pars = new HashMap<>();
        pars.put("login", mUser);
        pars.put("password", mPassword);
        return pars;
    }
}
