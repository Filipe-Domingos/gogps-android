package br.com.sd.go.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.sd.go.GoGPS;
import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.requests.CommandAPIRequest;

public class CommandRequests {

    private final String TAG = CommandRequests.class.getSimpleName();

    private ProgressDialog mProgressDialog;
    private MainActivity activity;
    private int command = CommandAPIRequest.BLOCK_COMMAND;
    private Long deviceId;

    CommandRequests(MainActivity activity, Long deviceId, int command) {
        this.activity = activity;
        this.deviceId = deviceId;
        this.command = command;
    }

    private void processarEnvio() {
        String message = this.command == CommandAPIRequest.BLOCK_COMMAND ?
                "corte" : "restauração";
        mProgressDialog = ProgressDialog.show(activity, "Aguarde",
                "Realizando " + message + "...");

        JSONObject object = new JSONObject();

        try {
            object.put("device_id", deviceId);
            object.put("cod_command", command);
        } catch (JSONException e) {
            message = "Ocorreu um erro ao enviar a solicitação.";
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            Log.e(TAG, message, e);
            return;
        }

        CommandAPIRequest request = new CommandAPIRequest(object,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (mProgressDialog != null && mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                        try {
                            String message = "Solicitação enviada com sucesso!";
                            if (response.has("message")) {
                                message = response.getString("message");
                            }
                            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            Log.e(TAG, "Error while reading response", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                String message = "Ocorreu um erro ao enviar a solicitação.";
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }
        });

        NetworkUtils.addToRequestQueue(request);
    }

    private void validarSenha() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Validar senha");
        View dialog = View.inflate(activity, R.layout.input_password, null);
        final EditText input = (EditText) dialog.findViewById(R.id.et_password);

        builder.setView(dialog);

        builder.setPositiveButton("Confimar senha", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String auth = GoGPS.getBasicAuth();
                String decodedAuth = new String(Base64.decode(auth, Base64.DEFAULT));
                String[] result = decodedAuth.split(":");
                if (result.length == 2) {
                    String rPassword = result[1];
                    if (rPassword.equals(input.getText().toString())) {
                        processarEnvio();
                    } else {
                        Toast.makeText(activity, "Não foi possível validar sua senha",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "Não foi possível validar sua senha",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void sinalizarTermos() {
        String message = this.command == CommandAPIRequest.BLOCK_COMMAND ?
                "bloquear" : "restaurar";

        String title = this.command == CommandAPIRequest.BLOCK_COMMAND ?
                "Bloquear" : "Restaurar";

        new AlertDialog.Builder(activity)
                .setTitle(title + " veículo")
                .setMessage("Deseja realmente " + message + " o veículo?\n" +
                        "Leia os termos antes de continuar essa ação.")
                .setIcon(R.drawable.ic_action_alert_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        validarSenha();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setNeutralButton(R.string.read_terms, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.openTermsFragment();
                    }
                }).show();
    }

    public void sendCommand() {
        sinalizarTermos();
    }
}
