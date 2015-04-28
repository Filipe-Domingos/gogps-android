package br.com.sd.go.adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import br.com.sd.go.GoGPS;
import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.models.VehicleItem;
import br.com.sd.go.requests.CommandAPIRequest;
import br.com.sd.go.utils.NetworkUtils;

public class ListCarsAdapter extends ArrayAdapter<VehicleItem> {

    private final String TAG = ListCarsAdapter.class.getSimpleName();
    private MainActivity activity;
    private ProgressDialog mProgressDialog;

    public ListCarsAdapter(Context context, List<VehicleItem> objects) {
        super(context, R.layout.item_list_cars, objects);
        activity = (MainActivity) context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_list_cars, parent, false);

            // initialize the view holder
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.llCortar = (LinearLayout) convertView.findViewById(R.id.cortar);
            viewHolder.llRestaurar = (LinearLayout) convertView.findViewById(R.id.restaurar);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        VehicleItem item = getItem(position);
        viewHolder.tvTitle.setText(item.getName());

        viewHolder.llCortar.setOnClickListener(new CommandClickListener(item.getId(),
                CommandAPIRequest.BLOCK_COMMAND));

        viewHolder.llRestaurar.setOnClickListener(new CommandClickListener(item.getId(),
                CommandAPIRequest.UNLOCK_COMMAND));

        return convertView;
    }

    private class CommandClickListener implements View.OnClickListener {

        int command = CommandAPIRequest.BLOCK_COMMAND;
        Long deviceId;

        CommandClickListener(Long deviceId, int command) {
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
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), "Não foi possível validar sua senha",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Não foi possível validar sua senha",
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
                    .setTitle(title +" veículo")
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

        @Override
        public void onClick(View v) {
            sinalizarTermos();
        }
    }

    private static class ViewHolder {
        TextView tvTitle;
        LinearLayout llCortar;
        LinearLayout llRestaurar;
    }
}
