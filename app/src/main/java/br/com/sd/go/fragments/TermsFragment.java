package br.com.sd.go.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.sd.go.R;
import br.com.sd.go.requests.TermsOfUseRequest;
import br.com.sd.go.utils.NetworkUtils;

public class TermsFragment extends Fragment {

    private static final String TAG = TermsFragment.class.getCanonicalName();

    private TextView mContent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = View.inflate(getActivity(), R.layout.fragment_terms, null);
        mContent = (TextView) view.findViewById(R.id.content_terms);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
    }

    public void updateView() {
        mContent.setText(R.string.loading);

        final TermsOfUseRequest request = new TermsOfUseRequest
                (new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        if (response.length() > 0) {
                            try {
                                JSONObject data = response.getJSONObject(response.length() - 1);
                                mContent.setText(data.getString("value"));
                            } catch (JSONException e) {
                                Log.e(TAG, "Error while reading marker from response", e);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "Verifique sua conexão.",
                                Toast.LENGTH_SHORT).show();
                        mContent.setText(R.string.verify_your_conection);
                    }
                });

        NetworkUtils.addToRequestQueue(request);
    }
}
