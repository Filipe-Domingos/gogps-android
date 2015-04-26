package br.com.sd.go;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import br.com.sd.go.requests.LoginRequest;
import br.com.sd.go.utils.NetworkUtils;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    EditText mEmail;
    EditText mPassword;
    Button mBtnLogin;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        String basicAuth = GoGPS.getBasicAuth();
        if (basicAuth != null) {
            startActivity(new Intent(getBaseContext(), MainActivity.class));
            finish();
        } else {
            mEmail = (EditText) findViewById(R.id.etUsuario);
            mPassword = (EditText) findViewById(R.id.etSenha);
            mBtnLogin = (Button) findViewById(R.id.btEntrar);

            mBtnLogin.setOnClickListener(this);
        }
    }

    public void onClick(View view) {
        if (validaDados()) {
            showProgressDialog();

            final String email = mEmail.getText().toString();
            final String password = mPassword.getText().toString();

            LoginRequest request = new LoginRequest(email, password,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            String formatted = email + ":" + password;
                            byte[] encoded = Base64.encode(formatted.getBytes(), Base64.DEFAULT);
                            GoGPS.setBasicAuth(new String(encoded));
                            hideProgressDialog();
                            startActivity(new Intent(getBaseContext(), MainActivity.class));
                            finish();
                        }
                    },

                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            hideProgressDialog();
                            Toast.makeText(LoginActivity.this, "Verifique seus dados.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            NetworkUtils.addToRequestQueue(request);
        }
    }

    private boolean validaDados() {
        if (mEmail.getText().toString().isEmpty() && mEmail.getText().toString().equals("")) {
            return false;
        }

        if (mPassword.getText().toString().isEmpty() && mPassword.getText().toString().equals("")) {
            return false;
        }

        return true;
    }

    public void showProgressDialog() {
        try {
            mProgressDialog = ProgressDialog.show(this, "Aguarde", "Realizando login...");
        } catch (Throwable e) {
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    public void hideProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        } catch (Throwable e) {
            Log.e("ERROR", e.getMessage(), e);
        }
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
