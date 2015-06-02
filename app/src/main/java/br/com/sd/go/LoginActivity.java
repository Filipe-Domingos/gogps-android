package br.com.sd.go;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

public class LoginActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    EditText mEmail;
    EditText mPassword;
    Button mBtnLogin;
    CheckBox mRemember;

    private ProgressDialog mProgressDialog;

    private Boolean mProcessingLogin = false;

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
            mRemember = (CheckBox) findViewById(R.id.cb_remember);

            mBtnLogin.setOnClickListener(this);
        }
    }

    public void onClick(View view) {
        if (validaDados() && !mProcessingLogin) {
            String email = mEmail.getText().toString();
            String password = mPassword.getText().toString();

            new LoginAsyncTask(email, password).execute();
        }
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private static final String sUrl = "http://gogps.com.br/gogps-rest/api/user/login";
        String mUser, mPassword;

        public LoginAsyncTask(String email, String password) {
            mUser = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<NameValuePair> postParameters;
            postParameters = new ArrayList<>();
            postParameters.add(new BasicNameValuePair("login", mUser));
            postParameters.add(new BasicNameValuePair("password", mPassword));

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(sUrl);

            HttpResponse httpResponse;

            try {
                String base64 = Base64.encodeToString((mUser + ":" + mPassword).getBytes(),
                        Base64.NO_WRAP);
                httpPost.addHeader("Authorization", "Basic " + base64);
                httpPost.setEntity(new UrlEncodedFormEntity(postParameters));
                httpResponse = httpclient.execute(httpPost);
            } catch (IOException e) {
                Log.e(TAG, "Error on request login", e);
                return false;
            }

            int responseCode = httpResponse.getStatusLine().getStatusCode();

            return responseCode == 200;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
            mProcessingLogin = true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            hideProgressDialog();
            if (success) {
                String formatted = mUser + ":" + mPassword;
                byte[] encoded = Base64.encode(formatted.getBytes(), Base64.DEFAULT);
                GoGPS.setBasicAuth(new String(encoded));
                GoGPS.setRemember(mRemember.isChecked());
                startActivity(new Intent(getBaseContext(), MainActivity.class));
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Verifique seus dados.",
                        Toast.LENGTH_SHORT).show();
            }
            mProcessingLogin = false;
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
