package br.com.sd.go.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.sd.go.MainActivity;
import br.com.sd.go.R;
import br.com.sd.go.adapters.ListCarsAdapter;
import br.com.sd.go.models.VehicleItem;
import br.com.sd.go.requests.CommandAPIRequest;
import br.com.sd.go.requests.DevicesUserRequest;
import br.com.sd.go.utils.NetworkUtils;

public class ListCarsFragment extends ListFragment {

    private static final String TAG = ListCarsFragment.class.getCanonicalName();

    private List<VehicleItem> mItems;
    private ProgressDialog mProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mItems = new ArrayList<>();
        loadData();
    }

    @Override
    public void onListItemClick(ListView listView, View view, final int position, long id) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Bloquear veículo")
                .setMessage("Deseja realmente bloquear o veículo?\n" +
                        "Leia os termos antes de continuar essa ação.")
                .setIcon(R.drawable.ic_action_alert_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        blockVehicle(mItems.get(position).getId());
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setNeutralButton(R.string.read_terms, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).openTermsFragment();
                    }
                }).show();
    }

    private void blockVehicle(Long deviceId) {
        mProgressDialog = ProgressDialog.show(getActivity(), "Aguarde", "Realizando bloqueio...");
        int command = CommandAPIRequest.BLOCK_COMMAND;

        JSONObject object = new JSONObject();

        try {
            object.put("device_id", deviceId);
            object.put("cod_command", command);
        } catch (JSONException e) {
            String message = "Ocorreu um erro ao enviar a solicitação.";
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });

        NetworkUtils.addToRequestQueue(request);
    }

    private void loadData() {
        DevicesUserRequest request = new DevicesUserRequest
                (new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        mItems.clear();

                        for (int i = 0; i < response.length(); ++i) {
                            try {
                                JSONObject data = response.getJSONObject(i);
                                Long id = data.getLong("id");
                                String name = data.getString("name");

                                mItems.add(new VehicleItem(id, name));
                            } catch (JSONException e) {
                                Log.e(TAG, "Error while reading marker from response", e);
                            }
                        }

                        Collections.sort(mItems);

                        setListAdapter(new ListCarsAdapter(getActivity(), mItems));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Verifique sua conexão.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        NetworkUtils.addToRequestQueue(request);
    }
}
