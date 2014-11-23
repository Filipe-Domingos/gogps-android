package br.com.sd.go;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

public class MainActivity extends Activity implements View.OnClickListener {

    EditText    usuario;
    EditText    senha;
    Button      btEntrar;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usuario = (EditText) findViewById(R.id.etUsuario);
        senha = (EditText) findViewById(R.id.etSenha);
        btEntrar = (Button) findViewById(R.id.btEntrar);

        btEntrar.setOnClickListener(this);
    }

    public void onClick(View view) {

        if( validaDados() ){
            if(isConnected()){
                new HttpAsyncTask(this).execute("http://gogps.com.br/login/"+usuario.getText()+"/"+senha.getText());
            } else {
                Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getBaseContext(), "Por favor, preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean validaDados() {
        if(usuario.getText().toString().isEmpty() && usuario.getText().toString().equals("")) {
            return false;
        }

        if(senha.getText().toString().isEmpty() && senha.getText().toString().equals("")) {
            return false;
        }

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

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
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
                Integer id = Integer.parseInt(result);

                if(id != 0) {
                    Intent it = new Intent(getBaseContext(), MapActivity.class);
                    it.putExtra("id", id);
                    startActivity(it);

                    finish();
                } else {
                    Toast.makeText(getBaseContext(), "Usuário e senha incorretos", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Usuário e senha incorretos", Toast.LENGTH_LONG).show();
            } finally {
                hideProgressDialog();
            }
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
